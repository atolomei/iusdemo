package io.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.demo.model.DemoObjectMapper;
import io.demo.model.DocumentAnalyze;
import io.demo.model.User;
import io.demo.model.db.service.DocumentAnalyzeDBService;
import io.demo.service.rag.DocumentAnalysisResponse;
import io.demo.util.Check;
import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

/**
 * Cache / log of document analysis executed on the {@link LegalSearchService}.
 * <p>
 * Every document analysis is logged in the database as a
 * {@link DocumentAnalyze} instance (PostgreSQL), with the
 * {@link DocumentAnalysisResponse} serialized as JSON in {@code results}. The
 * database is the source of truth: cached results are loaded from the
 * {@code documentanalyze} table, while an internal Caffeine cache is used only
 * as an in-memory performance layer.
 * </p>
 * <p>
 * The key of a cache entry is a hash of {@code ragDocumentId} and
 * {@code trim(toLowerCase(query))}, and the value is the
 * {@link DocumentAnalysisResponse} returned by the Kbee RAG Server.
 * </p>
 */
@Service
public class DocumentAnalyzeCacheService extends BaseService {

	static private io.demo.Logger logger = io.demo.Logger.getLogger(DocumentAnalyzeCacheService.class.getName());

	@Autowired
	DateTimeService dateService;

	private final DocumentAnalyzeDBService documentAnalyzeDBService;

	/** In-memory performance layer over the database log. */
	@JsonIgnore
	private Cache<String, DocumentAnalysisResponse> cache;

	/** Jackson 3 mapper used to (de)serialize the {@code results} JSON column. */
	@JsonIgnore
	private final ObjectMapper mapper = new DemoObjectMapper();

	public DocumentAnalyzeCacheService(Settings settings, DateTimeService dateService, DocumentAnalyzeDBService documentAnalyzeDBService) {
		super(settings);
		this.dateService = dateService;
		this.documentAnalyzeDBService = documentAnalyzeDBService;
	}

	@PostConstruct
	protected synchronized void onInitialize() {
		this.cache = Caffeine.newBuilder()
				.initialCapacity(getSettings().getQueryCacheInitialCapacity())
				.maximumSize(getSettings().getQueryCacheMaxCapacity())
				.expireAfterWrite(getSettings().getCacheQueryDurationMinutes(), TimeUnit.MINUTES)
				.build();
	}

	/**
	 * Returns the cached analysis for the document and query, if present.
	 * Looks first in the in-memory cache and then in the database log (only
	 * entries logged within the cache duration are considered valid).
	 */
	public Optional<DocumentAnalysisResponse> get(String ragDocumentId, String query) {
		if (ragDocumentId == null || query == null)
			return Optional.empty();

		String key = key(ragDocumentId, query);

		DocumentAnalysisResponse cached = getCache().getIfPresent(key);

		if (cached != null)
			return Optional.of(cached);

		// not in memory -> look up the most recent database entry
		try {
			DocumentAnalyze logged = documentAnalyzeDBService.getMostRecent(ragDocumentId.trim(), query.trim());
			if (logged == null || logged.getResults() == null)
				return Optional.empty();

			// entries older than the cache duration are not served from the log
			if (logged.getCreated() == null || logged.getCreated()
					.isBefore(OffsetDateTime.now().minusMinutes(getSettings().getCacheQueryDurationMinutes())))
				return Optional.empty();

			Optional<DocumentAnalysisResponse> response = parseResults(logged.getResults());
			if (response.isEmpty())
				return Optional.empty();

			getCache().put(key, response.get());
			return response;

		} catch (Exception e) {
			logger.error(e, "could not load cached document analysis from database -> " + ragDocumentId + " | " + query);
			return Optional.empty();
		}
	}

	/** Stores the analysis of the document and query in the cache (memory and database). */
	public void put(String ragDocumentId, String query, DocumentAnalysisResponse result, User user, String sessionId) {
		put(ragDocumentId, query, result, 0, user, sessionId);
	}
	

	/**
	 * Stores the analysis of the document and query in the cache (memory) and
	 * logs it in the database.
	 */
	public void put(String ragDocumentId, String query, DocumentAnalysisResponse result, long durationMillisecs, User user, String sessionId) {

		
		Check.requireNonNull(ragDocumentId, "ragDocumentId");
		Check.requireNonNull(query, "query is null");
		Check.requireNonNull(user, "user is null");
		Check.requireNonNull(sessionId, "sessionId is null");
		
		//if (ragDocumentId == null || query == null || result == null)
		//	return;

		getCache().put(key(ragDocumentId, query), result);
		log(ragDocumentId, query, result, durationMillisecs, user, sessionId);
	}

	/** Logs a document analysis in the database. */
	public DocumentAnalyze log(String ragDocumentId, String query, DocumentAnalysisResponse result, long durationMillisecs, User user, String sessionId) {
		
		Check.requireNonNull(ragDocumentId, "ragDocumentId");
		Check.requireNonNull(query, "query is null");
		Check.requireNonNull(user, "user is null");
		Check.requireNonNull(sessionId, "sessionId is null");
		
		try {
		
			DocumentAnalyze analyze = new DocumentAnalyze();

			analyze.setRagDocumentId(ragDocumentId.trim());
			analyze.setQuestion(query.trim());
			analyze.setResults(mapper.writeValueAsString(result));
			analyze.setDurationMillisecs(durationMillisecs);
			analyze.setSession_id(sessionId);
			analyze.setCreated(OffsetDateTime.now());
			analyze.setLastModified(OffsetDateTime.now());
			analyze.setLastModifiedUser(user);

			return documentAnalyzeDBService.save(analyze);

		} catch (Exception e) {
			logger.error(e, "could not log document analysis -> " + ragDocumentId + " | " + query);
			return null;
		}
	}

	/** Removes the in-memory entry for the document and query, if present. */
	public void invalidate(String ragDocumentId, String query) {
		if (ragDocumentId == null || query == null)
			return;
		getCache().invalidate(key(ragDocumentId, query));
	}

	/** Removes all in-memory entries (the database log is preserved). */
	public void invalidateAll() {
		getCache().invalidateAll();
	}

	/** Empties the in-memory cache (the database log is preserved). */
	public void cleanUp() {
		invalidateAll();
	}

	/**
	 * Parses the {@code results} column into a {@link DocumentAnalysisResponse}.
	 * Returns empty if the column does not contain a valid JSON object.
	 */
	protected Optional<DocumentAnalysisResponse> parseResults(String results) {
		String trimmed = results.trim();

		// quick sanity check: a serialized DocumentAnalysisResponse is a JSON object
		if (!trimmed.startsWith("{"))
			return Optional.empty();

		try {
			return Optional.ofNullable(mapper.readValue(trimmed, DocumentAnalysisResponse.class));
		} catch (Exception e) {
			logger.debug("logged results are not a valid DocumentAnalysisResponse JSON, ignoring entry: " + e.getMessage());
			return Optional.empty();
		}
	}

	/** Key of a cache entry: hash of ragDocumentId + trim(toLowerCase(query)). */
	protected String key(String ragDocumentId, String query) {
		String normalized = ragDocumentId.trim() + "|" + query.toLowerCase().trim();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(normalized.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 is always available; fall back just in case
			return normalized;
		}
	}

	public DateTimeService getDateTimeService() {
		return this.dateService;
	}

	public DocumentAnalyzeDBService getDocumentAnalyzeDBService() {
		return this.documentAnalyzeDBService;
	}

	private Cache<String, DocumentAnalysisResponse> getCache() {
		return this.cache;
	}

}
