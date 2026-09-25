package io.demo.results;

/**
 * Legal subject (materia) filter options for the search results.
 */
public enum SubjectOption {

	TODOS("Todos"),
	CIVIL("Civil"),
	PENAL("Penal"),
	LABORAL("Laboral"),
	COMERCIAL("Comercial"),
	ADMINISTRATIVO("Administrativo"),
	CONSTITUCIONAL("Constitucional"),
	TRIBUTARIO("Tributario"),
	AMBIENTAL("Ambiental"),
	FAMILIA("Familia"),
	OTROS("Otros");

	private final String label;

	SubjectOption(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	/** default selection for the selector */
	public static SubjectOption getDefault() {
		return TODOS;
	}

	/** Returns the SubjectOption with the given ordinal, or the default if out of range. */
	public static SubjectOption fromOrdinal(int ordinal) {
		SubjectOption[] values = values();
		return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : getDefault();
	}

	@Override
	public String toString() {
		return label;
	}
}
