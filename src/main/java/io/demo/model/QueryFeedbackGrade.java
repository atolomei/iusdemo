package io.demo.model;

/**
 * Grade of a {@link QueryFeedback}. The int value is stored in the
 * {@code grade} column of the {@code QueryFeedback} table (default -1 = no
 * grade).
 */
public enum QueryFeedbackGrade {

	EXCELLENT(1, "Excelente"),
	VERY_GOOD(2, "Muy bueno"),
	GOOD(3, "bueno. Me sirve pero hay contenido más útil"),
	BAD(4, "No me resulta útil"),
	NO_OPINION(5, "No puedo opinar");

	private final int value;
	private final String displayName;

	QueryFeedbackGrade(int value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

	public int getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}

	/** Returns the grade for the given int value, or null if none matches. */
	public static QueryFeedbackGrade fromValue(int value) {
		for (QueryFeedbackGrade g : values())
			if (g.value == value)
				return g;
		return null;
	}
}
