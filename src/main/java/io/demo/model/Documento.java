package io.demo.model;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter

@NoArgsConstructor
public class Documento extends JsonObject implements Serializable {

	
	
	private static final long serialVersionUID = 1L;

	@JsonProperty("id")
	private String id;

    @JsonProperty("title")
	private String title;
	
    @JsonProperty("subtitle")
	private String subtitle;

    /** Analysis of the document (shown in the expanded panel). */
    @JsonProperty("analysis")
	private String analysis;

    /** Quotes of the document (shown in the expanded panel). */
    @JsonProperty("quotes")
	private List<String> quotes = new ArrayList<>();

    /** Jurisprudential citations (shown in the expanded panel). */
    @JsonProperty("citasFallos")
	private List<String> citasFallos = new ArrayList<>();

    /** Normative citations (shown in the expanded panel). */
    @JsonProperty("citasNormas")
	private List<String> citasNormas = new ArrayList<>();

    @JsonProperty("ragDocumentId")
  	private String ragDocumentId;
    
    
    @JsonProperty("pjsfDocumentId")
  	private String pjsfDocumentId;
    
    
    @JsonProperty("ragResponseId")
  	private String ragResponseId;
    
    
    @JsonProperty("score")
	private double score;
    
    @JsonProperty("relevanceOrder")
	private  int relevanceOrder;

    
    public Documento(String id, String title) {
		this.id = id;
		this.title = title;
	}
	
	
}
