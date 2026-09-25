package io.demo.results;

/**
 * Ordering options for the search results.
 */
public enum OrderOption {

	MAS_RECIENTES("Más recientes"),
	MAS_RELEVANTES("Más relevantes");
	//MAYOR_COINCIDENCIA("Mayor coincidencia");

	private final String label;

	OrderOption(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	/** default selection for the selector */
	public static OrderOption getDefault() {
		return MAS_RELEVANTES;
	}

	@Override
	public String toString() {
		return label;
	}
}
