package io.demo.results;

/**
 * Relevance level of a result with respect to the query (see
 * {@code io.demo.service.rag.Source#queryLevel()}).
 */
public enum QueryLevelOption {

	RELEVANCIA_DIRECTA(0, "Relevancia directa"),
	CRITERIO_DIRECTAMENTE_APLICABLE(1, "Relevancia alta - Criterio aplicable"),
	RELEVANCIA_COMPLEMENTARIA_SUSTANTIVA(2, "Relevancia media - complementario");

	private final int level;
	private final String label;

	QueryLevelOption(int level, String label) {
		this.level = level;
		this.label = label;
	}

	public int getLevel() {
		return level;
	}

	public String getLabel() {
		return label;
	}

	/** @return the option for the level (0,1,2), or null if unknown */
	public static QueryLevelOption of(int level) {
		for (QueryLevelOption o : values())
			if (o.level == level)
				return o;
		return null;
	}

	@Override
	public String toString() {
		return label;
	}
}
