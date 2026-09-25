package io.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.demo.Logger;
import io.demo.model.DemoObjectMapper;
import io.demo.model.Query;
import io.demo.model.RAGSentencia;
import io.demo.model.User;
import io.demo.model.db.service.QueryDBService;
import io.demo.service.rag.RAGConverter;
import io.demo.service.rag.RagResponse;
import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

/**
 * Single query cache / log of the application.
 * <p>
 * Every query executed by the server is logged in the database as a
 * {@link Query} instance (PostgreSQL), with the JSON returned by the server
 * ({@code results}) and the round-trip duration ({@code durationMillisecs}).
 * The database is the source of truth: cached results are loaded from the
 * {@code query} table, while an internal Caffeine cache is used only as an
 * in-memory performance layer.
 * </p>
 * <p>
 * The key of a cache entry is a hash of {@code trim(toLowerCase(query))}, and
 * the value is the {@code List<Sentencia>} returned by the search.
 * </p>
 */
@Service
public class QueryLogService extends BaseService {

	static private Logger logger = Logger.getLogger(QueryLogService.class.getName());

	private final QueryDBService queryDBService;

	/** In-memory performance layer over the database log. */
	@JsonIgnore
	private Cache<String, List<RAGSentencia>> cache;

	/** Jackson mapper used to deserialize the {@code results} JSON column. */
	@JsonIgnore
	private final ObjectMapper mapper = new DemoObjectMapper();

	public QueryLogService(Settings settings, QueryDBService queryDBService) {
		super(settings);
		this.queryDBService = queryDBService;
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
	 * Logs a query in the database.
	 *
	 * @param queryText        the query text
	 * @param resultsJson      the JSON returned by the server
	 * @param durationMillisecs the round-trip duration in milliseconds
	 */
	public Query log(String queryText, String resultsJson, long durationMillisecs, User user, String sessionId) {
		return log(queryText, resultsJson, durationMillisecs, user, sessionId,
				io.demo.results.DateRange.getDefault(), io.demo.results.SubjectOption.getDefault(),
				io.demo.results.ReasoningEffortOption.getDefault());
	}

	/**
	 * Logs a query in the database with the toolbar filter options.
	 *
	 * @param queryText             the query text
	 * @param resultsJson           the JSON returned by the server
	 * @param durationMillisecs     the round-trip duration in milliseconds
	 * @param dateRange             selected date range (null -> default)
	 * @param subjectOption         selected subject (null -> default)
	 * @param reasoningEffortOption selected reasoning effort (null -> default)
	 */
	public Query log(String queryText, String resultsJson, long durationMillisecs, User user, String sessionId,
			io.demo.results.DateRange dateRange, io.demo.results.SubjectOption subjectOption,
			io.demo.results.ReasoningEffortOption reasoningEffortOption) {
		return log(queryText, resultsJson, null, durationMillisecs, user, sessionId, dateRange, subjectOption, reasoningEffortOption);
	}

	/**
	 * Same as {@link #log(String, String, long, User, String, io.demo.results.DateRange, io.demo.results.SubjectOption, io.demo.results.ReasoningEffortOption)},
	 * but also stores the server's id of the query ({@code serverId}), received
	 * from the Kbee RAG Server in the {@code RagResponse}.
	 */
	public Query log(String queryText, String resultsJson, String serverId, long durationMillisecs, User user, String sessionId,
			io.demo.results.DateRange dateRange, io.demo.results.SubjectOption subjectOption,
			io.demo.results.ReasoningEffortOption reasoningEffortOption) {
		try {
			Query query = new Query();

			query.setQuery(queryText);
			query.setResults(resultsJson);
			query.setServerId(serverId);
			query.setDurationMillisecs(durationMillisecs);
			query.setSession_id(sessionId);
			query.setCreated(OffsetDateTime.now());
			query.setLastModified(OffsetDateTime.now());

			query.setLastModifiedUser(user);

			io.demo.results.ReasoningEffortOption effort =
					(reasoningEffortOption != null ? reasoningEffortOption : io.demo.results.ReasoningEffortOption.getDefault());

			query.setDateRangeOption((dateRange != null ? dateRange : io.demo.results.DateRange.getDefault()).ordinal());
			query.setSubjectOption((subjectOption != null ? subjectOption : io.demo.results.SubjectOption.getDefault()).ordinal());
			query.setTotalOption(effort.getTopK());
			query.setReasoningEffortOption(effort.ordinal());

			// identity key of the query: text + date range + subject + reasoning effort
			query.setQueryKey(queryKey(queryText, dateRange, subjectOption, reasoningEffortOption));

			return queryDBService.save(query);

		} catch (Exception e) {
			logger.error(e, "could not log query -> " + queryText);
			return null;
		}
	}

	/**
	 * Returns the cached result for the query, if present. Looks first in the
	 * in-memory cache and then in the database log (only entries logged within
	 * the cache duration are considered valid).
	 */
	public Optional<List<RAGSentencia>> get(String query) {
		if (query == null)
			return Optional.empty();

		String key = key(query);

		List<RAGSentencia> cached = getCache().getIfPresent(key);
		if (cached != null)
			return Optional.of(cached);

		// not in memory -> look up the most recent database entry
		try {
			Query logged = queryDBService.getMostRecentByText(query);
			if (logged == null || logged.getResults() == null)
				return Optional.empty();

			// entries older than the cache duration are not served from the log
			if (logged.getCreated() == null || logged.getCreated()
					.isBefore(OffsetDateTime.now().minusMinutes(getSettings().getCacheQueryDurationMinutes())))
				return Optional.empty();

			// not all logged entries contain a RagResponse JSON (e.g. legacy
			// rows logged with a plain-text answer) -> treat those as a miss
			Optional<RagResponse> response = parseResults(logged.getResults());
			if (response.isEmpty())
				return Optional.empty();

			List<RAGSentencia> result = new RAGConverter(response.get()).convert();

			getCache().put(key, result);
			return Optional.of(result);

		} catch (Exception e) {
			logger.error(e, "could not load cached query from database -> " + query);
			return Optional.empty();
		}
	}

	/**
	 * Parses the {@code results} column into a {@link RagResponse}.
	 * Returns empty if the column does not contain a valid RagResponse JSON
	 * (some rows in the log contain plain text instead of JSON).
	 */
	protected Optional<RagResponse> parseResults(String results) {
		String trimmed = results.trim();

		// quick sanity check: a serialized RagResponse is a JSON object
		if (!trimmed.startsWith("{"))
			return Optional.empty();

		try {
			return Optional.ofNullable(mapper.readValue(trimmed, RagResponse.class));
		} catch (Exception e) {
			logger.debug("logged results are not a valid RagResponse JSON, ignoring entry: " + e.getMessage());
			return Optional.empty();
		}
	}

	/** Stores the result of a query in the in-memory cache. */
	public void put(String query, List<RAGSentencia> result) {
		if (query == null || result == null)
			return;
		getCache().put(key(query), result);
	}

	/**
	 * Stores the result of a query in the in-memory cache, keyed by the full
	 * query identity (text + date range + subject + reasoning effort).
	 */
	public void put(String query, io.demo.results.DateRange dateRange, io.demo.results.SubjectOption subjectOption,
			io.demo.results.ReasoningEffortOption reasoningEffortOption, List<RAGSentencia> result) {
		if (query == null || result == null)
			return;
		getCache().put(queryKey(query, dateRange, subjectOption, reasoningEffortOption), result);
	}

	/**
	 * Returns the cached result for the query identified by text + date range +
	 * subject + reasoning effort, if present. Looks first in the in-memory
	 * cache and then in the database log (by {@code query_key}, only entries
	 * logged within the cache duration are considered valid).
	 */
	public Optional<List<RAGSentencia>> get(String query, io.demo.results.DateRange dateRange,
			io.demo.results.SubjectOption subjectOption, io.demo.results.ReasoningEffortOption reasoningEffortOption) {
		if (query == null)
			return Optional.empty();

		String key = queryKey(query, dateRange, subjectOption, reasoningEffortOption);

		List<RAGSentencia> cached = getCache().getIfPresent(key);
		if (cached != null)
			return Optional.of(cached);

		// not in memory -> look up the most recent database entry by key
		try {
			Query logged = queryDBService.getMostRecentByKey(key);
			if (logged == null || logged.getResults() == null)
				return Optional.empty();

			// entries older than the cache duration are not served from the log
			if (logged.getCreated() == null || logged.getCreated()
					.isBefore(OffsetDateTime.now().minusMinutes(getSettings().getCacheQueryDurationMinutes())))
				return Optional.empty();

			Optional<RagResponse> response = parseResults(logged.getResults());
			if (response.isEmpty())
				return Optional.empty();

			List<RAGSentencia> result = new RAGConverter(response.get()).convert();

			getCache().put(key, result);
			return Optional.of(result);

		} catch (Exception e) {
			logger.error(e, "could not load cached query from database -> " + query);
			return Optional.empty();
		}
	}

	/**
	 * Identity key of a query: SHA-256 hash of the normalized query text plus
	 * the toolbar options that define the result set (date range, subject and
	 * reasoning effort). Null options are replaced by their defaults, so
	 * equivalent queries always produce the same key.
	 */
	public String queryKey(String query, io.demo.results.DateRange dateRange,
			io.demo.results.SubjectOption subjectOption, io.demo.results.ReasoningEffortOption reasoningEffortOption) {

		io.demo.results.DateRange dr = (dateRange != null) ? dateRange : io.demo.results.DateRange.getDefault();
		io.demo.results.SubjectOption so = (subjectOption != null) ? subjectOption : io.demo.results.SubjectOption.getDefault();
		io.demo.results.ReasoningEffortOption re = (reasoningEffortOption != null) ? reasoningEffortOption
				: io.demo.results.ReasoningEffortOption.getDefault();

		String canonical = query.toLowerCase().trim().replaceAll("\\s+", " ")
				+ "|" + dr.name()
				+ "|" + so.name()
				+ "|" + re.name();

		return sha256(canonical);
	}

	/** Removes the in-memory entry for the query, if present. */
	public void invalidate(String query) {
		if (query == null)
			return;
		getCache().invalidate(key(query));
	}

	/** Removes all in-memory entries (the database log is preserved). */
	public void invalidateAll() {
		getCache().invalidateAll();
	}

	/** Empties the in-memory cache (the database log is preserved). */
	public void cleanUp() {
		invalidateAll();
	}

	/** Key of a cache entry: hash of trim(toLowerCase(query)). */
	protected String key(String query) {
		return sha256(query.toLowerCase().trim());
	}

	private static String sha256(String text) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 is always available; fall back just in case
			return text;
		}
	}

	private Cache<String, List<RAGSentencia>> getCache() {
		return this.cache;
	}

	public QueryDBService getQueryDBService() {
		return queryDBService;
	}
}