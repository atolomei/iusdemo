package io.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.demo.Logger;
import jakarta.annotation.PostConstruct;

/**
 * Service containing a map of test queries.
 * <p>
 * The map key is the query ({@code trim(toLowerCase(query))}) and the value is
 * the {@code documentId} of the expected relevant sentencia. The map is
 * populated on {@link PostConstruct} from the JSON files in the
 * "testqueries" directory provided by {@link Settings}.
 * </p>
 */
@Service
public class TestQueriesService extends BaseService {

	static private Logger logger = Logger.getLogger(TestQueriesService.class.getName());

	/** Key: trim(toLowerCase(query)) -> documentId of the expected relevant sentencia. */
	private Map<String, String> testQueries = new HashMap<>();

	private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

	public TestQueriesService(Settings settings) {
		super(settings);
	}

	@PostConstruct
	protected synchronized void onInitialize() {

		// create the directory if it does not exist
		try {
			FileUtils.forceMkdir(getTestQueriesDir());
		} catch (IOException e) {
			throw new RuntimeException("Failed to create test queries directory: " + getTestQueriesDir(), e);
		}

		loadFromDisk();
	}

	/**
	 * @return {@code true} if the map contains the entry
	 *         {@code trim(toLowerCase(query)) -> documentId}
	 */
	public boolean check(String query, String documentId) {
		if (query == null || documentId == null)
			return false;
		String expected = this.testQueries.get(key(query));
		return documentId.equals(expected);
	}

	public Map<String, String> getTestQueries() {
		return this.testQueries;
	}

	/** Loads all the JSON files of the "testqueries" directory into the map. */
	protected void loadFromDisk() {
		File[] files = getTestQueriesDir().listFiles((d, name) -> name.endsWith(".json"));
		if (files == null)
			return;
		for (File file : files) {
			try {
				Map<String, String> entries = mapper.readValue(file, new TypeReference<Map<String, String>>() {});
				entries.forEach((query, documentId) -> this.testQueries.put(key(query), documentId));
			} catch (IOException e) {
				logger.error(e);
			}
		}
		logger.debug("test queries loaded -> " + this.testQueries.size());
	}

	/** Key of a map entry: trim(toLowerCase(query)). */
	protected String key(String query) {
		return query.toLowerCase().trim();
	}

	/** Directory provided by the {@link Settings} service ("testqueries"). */
	protected File getTestQueriesDir() {
		return new File(getSettings().getTestQueriesDir());
	}
}
