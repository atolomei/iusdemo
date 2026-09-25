package io.demo.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import io.demo.Logger;
import io.demo.model.ObjectState;
import io.demo.model.PJSFSentencia;
import io.demo.model.Query;
import io.demo.model.User;
import io.demo.model.db.service.PJSFSentenciaDBService;
import io.demo.service.rag.DocumentAnalysisResponse;
import io.demo.util.Check;
import jakarta.annotation.PreDestroy;

/**
 * Access point to {@link PJSFSentencia}: tries to get the sentencia from the
 * PostgreSQL database and, if it is not stored, retrieves the content from the
 * web ({@link PJSFSentenciaParserService}), parses it and stores it on the
 * database ({@link PJSFSentenciaDBService}).
 *
 * It also supports asynchronous prefetch of the document analysis
 * ({@link LegalSearchService#analyzeDocumentResponse}): the
 * {@code AnalysisPanel} triggers the prefetch when it is initialized, so that
 * while the user is reading the sumarios the analysis (citas, relevance) is
 * already being computed. When the user clicks the links, a blocking call
 * joins the pending future (or hits the cache).
 */
@Service
public class PJSFSentenciaService extends BaseService {

	static private Logger logger = Logger.getLogger(PJSFSentenciaService.class.getName());

	private final PJSFSentenciaDBService dbService;
	private final PJSFSentenciaParserService parserService;
	private final LegalSearchService legalSearchService;

	/** executor used for the asynchronous analysis prefetch */
	private final ExecutorService executor = Executors.newFixedThreadPool(4);

	/** pending analysis futures, keyed by ragDocumentId + query (dedupe) */
	private final ConcurrentHashMap<String, CompletableFuture<DocumentAnalysisResponse>> pending = new ConcurrentHashMap<>();

	public PJSFSentenciaService(Settings settings, PJSFSentenciaDBService dbService, PJSFSentenciaParserService parserService, LegalSearchService legalSearchService) {
		super(settings);
		this.dbService = dbService;
		this.parserService = parserService;
		this.legalSearchService = legalSearchService;
	}

	/**
	 * Returns the {@link PJSFSentencia} for the given PJSF document id (sid).
	 * If it is not stored in the database, the content is retrieved from the
	 * web, parsed and stored.
	 */
	public Optional<PJSFSentencia> getSentencia(String sid, User user) {

		Check.requireNonNull(sid, "sid is null");

		Optional<PJSFSentencia> stored = dbService.getByDocumentoId(sid);
		if (stored.isPresent())
			return stored;

		try {
			PJSFSentencia sentencia = parserService.fetch(sid);
			sentencia.setCreated(OffsetDateTime.now());
			sentencia.setState(ObjectState.PUBLISHED);
			User u = (user != null) ? user : dbService.getUserDBService().findRoot();
			sentencia.setLastModifiedUser(u);
			return Optional.of(dbService.save(sentencia, u));
		} catch (Exception e) {
			logger.error(e, "Error retrieving/parsing PJSF sentencia " + sid);
			return Optional.empty();
		}
	}

	// --- asynchronous analysis prefetch -----------------------------------

	/**
	 * Starts the analysis of the document asynchronously (non blocking). The
	 * result is cached by {@link LegalSearchService}, so a later blocking call
	 * to {@link #getAnalysis} is cheap.
	 */
	public void prefetchAnalysis(String ragDocumentId, Query query, User user, String sessionId) {

		if (ragDocumentId == null || ragDocumentId.isBlank() || query == null )
			return;

		logger.debug("prefetchAnalysis: ragDocumentId=" + ragDocumentId + " query=" + query);
		
		pending.computeIfAbsent(key(ragDocumentId, query.getId().toString()), k -> CompletableFuture.supplyAsync(() -> {
			try {
				return legalSearchService.analyzeDocumentResponse(ragDocumentId, query, user, sessionId);
			} finally {
				// remove after completion: subsequent calls hit the LegalSearchService cache
				pending.remove(k);
			}
		}, executor));
	}

	/**
	 * Blocking call: returns the analysis of the document, joining the pending
	 * prefetch if there is one (otherwise the call hits the cache or the RAG
	 * server directly).
	 */
	public DocumentAnalysisResponse getAnalysis(String ragDocumentId, Query query, User user, String sessionId) {

		CompletableFuture<DocumentAnalysisResponse> future = pending.get(key(ragDocumentId, query.getId().toString()));

		if (future != null) {
			logger.debug("future ok");
			return future.join();
		}
		
		return legalSearchService.analyzeDocumentResponse(ragDocumentId, query, user, sessionId);
	}

	private String key(String ragDocumentId, String query) {
		return ragDocumentId + "||" + query;
	}

	@PreDestroy
	public void shutdown() {
		executor.shutdown();
	}

}
