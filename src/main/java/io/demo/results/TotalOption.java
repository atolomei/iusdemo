package io.demo.results;

/**
 * Maximum number of results to display.
 */
public enum TotalOption {

	UP_TO_5("hasta 5", 5),
	UP_TO_10("hasta 10", 10),
	UP_TO_20("hasta 20", 20);

	private final String label;
	private final int max;

	TotalOption(String label, int max) {
		this.label = label;
		this.max = max;
	}

	public String getLabel() {
		return label;
	}

	public int getMax() {
		return max;
	}

	/** default selection for the selector */
	public static TotalOption getDefault() {
		return UP_TO_10;
	}

	@Override
	public String toString() {
		return label;
	}
}
