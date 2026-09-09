package io.demo.service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import io.demo.Logger;
import io.demo.model.Query;
import io.demo.model.User;
import io.demo.model.db.service.QueryDBService;

/**
 * Service to log user queries. For every query executed by the server an
 * instance of {@link Query} is inserted in the database, with the JSON
 * returned by the server ({@code results}) and the round-trip duration
 * ({@code durationMillisecs}).
 */
@Service
public class QueryLogService extends BaseService {

	static private Logger logger = Logger.getLogger(QueryLogService.class.getName());

	private final QueryDBService queryDBService;

	public QueryLogService(Settings settings, QueryDBService queryDBService) {
		super(settings);
		this.queryDBService = queryDBService;
	}

	/**
	 * Logs a query in the database.
	 *
	 * @param queryText        the query text
	 * @param resultsJson      the JSON returned by the server
	 * @param durationMillisecs the round-trip duration in milliseconds
	 */
	public Query log(String queryText, String resultsJson, long durationMillisecs, User user, String sessionId) {
		try {
			Query query = new Query();
			query.setQuery(queryText);
			query.setResults(resultsJson);
			query.setDurationMillisecs(durationMillisecs);
			query.setSession_id(sessionId);
			query.setCreated(OffsetDateTime.now());
			query.setLastModified(OffsetDateTime.now());
			
			query.setLastModifiedUser(user);
			
			
			return queryDBService.save(query);

		} catch (Exception e) {
			logger.error(e, "could not log query -> " + queryText);
			return null;
		}
	}

	/** Returns the Wicket session id, if there is a session bound to this thread. 
	 * 
	 * @return
	 
	private String getSessionId() {
		try {
			if (org.apache.wicket.Session.exists())
				return org.apache.wicket.Session.get().getId();
		} catch (Exception e) {
			logger.debug("no wicket session available");
		}
		return null;
	}
*/
	
	public QueryDBService getQueryDBService() {
		return queryDBService;
	}
}
