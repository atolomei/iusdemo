package io.demo.service;

import java.io.Serializable;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

/**
 * User preferences for the search UI.
 * <p>
 * Session-scoped: each user (HTTP session) has its own settings. Edited via
 * the {@code UserSettingsEditor} (page {@code /usersettings}) and used by the
 * {@code ResultsPanel} and {@code DemoHomePage} to configure how search
 * results are retrieved and displayed.
 * </p>
 */
@Service
@SessionScope
public class UserSettingsService implements Serializable {

	private static final long serialVersionUID = 1L;

	///** Whether the query cache of {@link LegalSearchService} is used. */
	//private boolean useQueryCache = true;

	/** Max number of results displayed by the ResultsPanel. */
	private int maxSearchResults = 5;

	/** Whether results are sorted by date, most recent first. */
	private boolean recentFirst = false;

	/** Max number of entries of the query history displayed. */
	private int maxHistory = 50;

	/** Whether the analysis is displayed in the expanded panel. */
	private boolean showAnalysis = true;

	/** Whether the quotes are displayed in the expanded panel. */
	private boolean showQuotes = false;

	/** Max number of quotes displayed in the expanded panel. */
	private int maxQuotes = 3;

	//public boolean isUseQueryCache() {
	//	return useQueryCache;
	//}

	//public void setUseQueryCache(boolean useQueryCache) {
	//	this.useQueryCache = useQueryCache;
	//}

	public int getMaxSearchResults() {
		return maxSearchResults;
	}

	public void setMaxSearchResults(int maxSearchResults) {
		this.maxSearchResults = maxSearchResults;
	}

	public boolean isRecentFirst() {
		return recentFirst;
	}

	public void setRecentFirst(boolean recentFirst) {
		this.recentFirst = recentFirst;
	}

	public int getMaxHistory() {
		return maxHistory;
	}

	public void setMaxHistory(int maxHistory) {
		this.maxHistory = maxHistory;
	}

	public boolean isShowAnalysis() {
		return showAnalysis;
	}

	public void setShowAnalysis(boolean showAnalysis) {
		this.showAnalysis = showAnalysis;
	}

	public boolean isShowQuotes() {
		return showQuotes;
	}

	public void setShowQuotes(boolean showQuotes) {
		this.showQuotes = showQuotes;
	}

	public int getMaxQuotes() {
		return maxQuotes;
	}

	public void setMaxQuotes(int maxQuotes) {
		this.maxQuotes = maxQuotes;
	}
}
