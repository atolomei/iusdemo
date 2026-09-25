package io.demo.results;

/**
 * Maximum number of results to display.
 */
public enum TotalOption {

	UP_TO_5("hasta 5", 5),
	UP_TO_10("hasta 10", 10),
	UP_TO_15("hasta 15", 15),
	UP_TO_20("hasta 20", 20),
	UP_TO_30("hasta 30", 30);
	

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

	/** Returns the TotalOption with the given max value, or the default if none matches. */
	public static TotalOption fromMax(int max) {
		for (TotalOption option : values())
			if (option.getMax() == max)
				return option;
		return getDefault();
	}

	@Override
	public String toString() {
		return label;
	}
}
