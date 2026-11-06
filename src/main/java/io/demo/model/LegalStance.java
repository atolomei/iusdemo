package io.demo.model;

import java.util.ResourceBundle;

import com.fasterxml.jackson.annotation.JsonCreator;

 

public enum LegalStance {

	
	
	
	FAVORABLE (0, "favorable"),
    CONTRARIO(1, "contrario"),
    MIXTO (2, "mixto"),
    NO_DEFINIDO( 3, "nodefinido");

	
	private final int level;
	private final String label;
	
	LegalStance(int level, String label) {
		this.level = level;
		this.label = label;
	}

	public int getLevel() {
		return level;
	}

	public String getLabel() {
		ResourceBundle bundle = ResourceBundle.getBundle(this.getClass().getName());
		return bundle.getString(label);
	}

	/** @return the option for the level (0,1,2,3), or null if unknown */
	public static LegalStance of(int level) {
		for (LegalStance o : values())
			if (o.level == level)
				return o;
		return null;
	}

	/**
	 * Lenient parser used by Jackson: accepts the server enum name ("CONTRARIO",
	 * "NO_DEFINIDO") as well as the label ("contrario", "no definido"),
	 * case-insensitive. Null, blank or unknown values map to {@link #NO_DEFINIDO}.
	 */
	@JsonCreator
	public static LegalStance fromString(String value) {
		if (value == null || value.isBlank())
			return NO_DEFINIDO;
		String v = value.trim();
		for (LegalStance o : values())
			if (o.name().equalsIgnoreCase(v) || o.label.equalsIgnoreCase(v) || o.name().equalsIgnoreCase(v.replace(' ', '_')))
				return o;
		return NO_DEFINIDO;
	}

	@Override
	public String toString() {
		return label;
	}
	
}