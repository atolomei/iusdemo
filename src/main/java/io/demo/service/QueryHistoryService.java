package io.demo.service;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

/**
 * History of queries executed by the current user, most recent first.
 * <p>
 * Session-scoped: each user (HTTP session) has its own history. Used by the
 * SearchForm to display previous queries and allow re-executing them.
 * </p>
 * <p>
 * Queries are deduplicated (re-executing a query moves it to the top) and the
 * history is capped at {@link #MAX_ENTRIES}.
 * </p>
 */
@Service
@SessionScope
public class QueryHistoryService implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int MAX_ENTRIES = 20;

	/** Most recent first. */
	private final Deque<String> history = new ArrayDeque<>();

	/** Records a query as executed (moves it to the top if already present). */
	public synchronized void record(String query) {
		if (query == null)
			return;
		String normalized = query.trim();
		if (normalized.isEmpty())
			return;
		// dedupe: same query (ignoring case and extra whitespace) must appear only once
		String key = key(normalized);
		history.removeIf(q -> key(q).equals(key));
		history.addFirst(normalized);
		while (history.size() > MAX_ENTRIES)
			history.removeLast();
	}

	/**
	 * Normalization used to decide whether two queries are "the same":
	 * lowercase, trimmed, inner whitespace collapsed.
	 */
	private static String key(String query) {
		return query.toLowerCase().trim().replaceAll("\\s+", " ");
	}

	/** Returns the queries executed by this user, most recent first. */
	public synchronized List<String> getHistory() {
		return new ArrayList<>(history);
	}

	public synchronized void clear() {
		history.clear();
	}

	public synchronized boolean isEmpty() {
		return history.isEmpty();
	}
}
