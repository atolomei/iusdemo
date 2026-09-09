package io.demo.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;

import io.demo.model.DemoObjectMapper;
import io.demo.model.Sentencia;
import jakarta.annotation.PostConstruct;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Cache of queries executed on the {@link LegalSearchService}.
 * <p>
 * The key of a cache entry is a hash of {@code trim(toLowerCase(query))}, and
 * the value is the {@code List<Sentencia>} returned by the search.
 * </p>
 */
@Service
public class QueryCacheService extends BaseService {

	static private io.demo.Logger logger = io.demo.Logger.getLogger(QueryCacheService.class.getName());

	@Autowired
	DateTimeService dateService;

	@JsonIgnore
	private Cache<String, List<Sentencia>> cache;

	/** Jackson mapper used to persist cache entries on disk. */
	@JsonIgnore
	private final ObjectMapper mapper = new DemoObjectMapper();
	
	public QueryCacheService(Settings settings, DateTimeService dateService) {
		super(settings);
		this.dateService = dateService;
	}

	@PostConstruct
	protected synchronized void onInitialize() {
		this.cache = Caffeine.newBuilder()
				.initialCapacity(getSettings().getQueryCacheInitialCapacity())
				.maximumSize(getSettings().getQueryCacheMaxCapacity())
				.expireAfterWrite(getSettings().getCacheQueryDurationMinutes(), TimeUnit.MINUTES)
				.evictionListener((key, value, cause) -> onRemoval(key, value, cause))
				.removalListener((key, value, cause) -> onRemoval(key, value, cause))
				.build();

		// create the persistence directory if it does not exist and load
		// the persisted entries into the cache
		try {
			FileUtils.forceMkdir(getCacheDir());
		} catch (IOException e) {
			throw new RuntimeException("Failed to create cache directory: " + getCacheDir(), e);
		}
		loadFromDisk();
	}

	/** Returns the cached result for the query, if present. */
	public Optional<List<Sentencia>> get(String query) {
		if (query == null)
			return Optional.empty();
		return Optional.ofNullable(getCache().getIfPresent(key(query)));
	}

	/** Stores the result of the query in the cache (memory and disk). */
	public void put(String query, List<Sentencia> result) {
		if (query == null || result == null)
			return;
		String key = key(query);
		getCache().put(key, result);
		writeToDisk(key, result);
	}

	/** Removes the entry for the query, if present (memory and disk). */
	public void invalidate(String query) {
		if (query == null)
			return;
		String key = key(query);
		getCache().invalidate(key);
		FileUtils.deleteQuietly(file(key));
	}

	/** Removes all entries (memory and disk). */
	public void invalidateAll() {
		getCache().invalidateAll();
		cleanUp();
	}

	/** Empties the persistence directory. */
	public void cleanUp() {
		getCache().invalidateAll();
		try {
			File dir = getCacheDir();
			if (dir.exists())
				FileUtils.cleanDirectory(dir);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	/** Loads all persisted entries from the persistence directory. */
	protected void loadFromDisk() {
		File dir = getCacheDir();
		File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
		if (files == null)
			return;
		for (File file : files) {
			try {
				List<Sentencia> value = mapper.readValue(file, new TypeReference<List<Sentencia>>() {});
				String key = file.getName().substring(0, file.getName().length() - ".json".length());
				getCache().put(key, value);
			} catch (Exception e) {
				logger.error(e);
				FileUtils.deleteQuietly(file);
			}
		}
	}

	/** Persists an entry on disk. */
	protected void writeToDisk(String key, List<Sentencia> value) {
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(file(key), value);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/** File of the entry with the given key. */
	protected File file(String key) {
		return new File(getCacheDir(), key + ".json");
	}

	/** Directory provided by the {@link Settings} service ("query"). */
	protected File getCacheDir() {
		return new File(getSettings().getQueryCacheDir());
	}

	/** Key of a cache entry: hash of trim(toLowerCase(query)). */
	protected String key(String query) {
		String normalized = query.toLowerCase().trim();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(normalized.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 is always available; fall back just in case
			return normalized;
		}
	}

	protected void onRemoval(Object key, Object value, RemovalCause cause) {
		if (cause.wasEvicted()) {

		}
	}

	public DateTimeService getDateTimeService() {
		return this.dateService;
	}

	private Cache<String, List<Sentencia>> getCache() {
		return this.cache;
	}

}
