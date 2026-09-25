package io.demo.results;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Reasoning effort selected in the search form. Each option carries the
 * key sent to the Kbee RAG Server ({@code reasoning_effort}) and the
 * {@code topK} value used for the search.
 */
public enum ReasoningEffortOption {

	LOW("low", 40),
	MEDIUM("medium", 60),
	HIGH("high", 80),
	XHIGH("xhigh", 100);
	//MAX("max", 60);

	private final String key;
	private final int topK;

	ReasoningEffortOption(String key, int topK) {
		this.key = key;
		this.topK = topK;
	}

	public String getKey() {
		return key;
	}

	public int getTopK() {
		return topK;
	}

	/** label displayed in the selector */
	public String getLabel() {
		ResourceBundle res = ResourceBundle.getBundle(ReasoningEffortOption.this.getClass().getName(), Locale.getDefault());
		return res.getString(this.key);
	}

	/** default selection for the selector */
	public static ReasoningEffortOption getDefault() {
		return HIGH;
	}

	/** Returns the option with the given ordinal, or the default if out of range. */
	public static ReasoningEffortOption fromOrdinal(int ordinal) {
		ReasoningEffortOption[] values = values();
		if (ordinal < 0 || ordinal >= values.length)
			return getDefault();
		return values[ordinal];
	}

	/** Returns the option with the given key, or the default if none matches. */
	public static ReasoningEffortOption fromKey(String key) {
		if (key != null)
			for (ReasoningEffortOption option : values())
				if (option.key.equalsIgnoreCase(key))
					return option;
		return getDefault();
	}

	@Override
	public String toString() {
		return key;
	}
}
