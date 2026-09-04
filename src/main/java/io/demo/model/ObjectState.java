package io.demo.model;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * 
 * 
 * 
 * 
 * ObjectSource
 * ------------
 * 
 * HumanMadeObject
 *      ArtWorkObject
 *      HistoricObject
 *      
 * NoHumanMadeObject
 * 
 * 
 */
public enum ObjectState {
	
	DRAFT (0, "draft"),
	EDITION(1, "edition"),
	APPROVED(2, "approved"),
	PUBLISHED(3, "published"),
	ARCHIVED(4, "archived"),
	DELETED(5, "deleted");	
	
	private final String label;
	private final int id;
	
	private ObjectState(int code, String label) {
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
		ResourceBundle res = ResourceBundle.getBundle(ObjectState.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	
	

	
}
