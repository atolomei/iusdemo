package io.demo.model;

import java.util.Locale;
import java.util.ResourceBundle;

public enum Role {

	
	SYSADMIN (0, "sysadmin"),
	ADMIN(1, "admin"),
	REGULAR_USER(2, "regular");	
	
	private final String label;
	private final int id;
	
	private Role(int code, String label) {
		this.label = label;
		this.id = code; 
	}
	
	public int getId() {
		return id;
	}

	public String toString() {
		return getLabel();
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}

	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(Role.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	
}
