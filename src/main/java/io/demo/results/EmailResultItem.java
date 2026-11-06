package io.demo.results;

import java.io.Serializable;

/**
 * Lightweight, read-only item used to build the email sent from the
 * {@link ResultsPanel} ("enviar por email"): title, subtitle (metadata and
 * relevance level, same text shown in the results list) and the public link
 * to the document.
 */
public class EmailResultItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String title;
	private final String subtitle;
	private final String link;

	/**
	 * Analysis text answering "¿Por qué es relevante?" for this document
	 * (same content shown by the {@code AnalysisPanel} "Por qué es relevante ?"
	 * link). Only populated for the first (most relevant) result of the email,
	 * since computing it requires an extra, potentially slow, call to the RAG
	 * server.
	 */
	private final String whyRelevant;

	public EmailResultItem(String title, String subtitle, String link) {
		this(title, subtitle, link, null);
	}

	public EmailResultItem(String title, String subtitle, String link, String whyRelevant) {
		this.title = title;
		this.subtitle = subtitle;
		this.link = link;
		this.whyRelevant = whyRelevant;
	}

	public String getTitle() {
		return title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public String getLink() {
		return link;
	}

	public String getWhyRelevant() {
		return whyRelevant;
	}
}
