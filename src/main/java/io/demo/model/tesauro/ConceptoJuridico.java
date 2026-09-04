package io.demo.model.tesauro;

import java.util.HashSet;
import java.util.Set;

import io.demo.model.JsonObject;

public class ConceptoJuridico extends JsonObject {

	
	 private Long id;

	    // Ej.: "Responsabilidad civil"
	    private String termino;


	    // Código estable del tesauro
	    private String codigo;
	    
	    // Definición o nota de alcance
	    private String definicion;

	    // Sinónimos, variantes, abreviaturas, etc.
	    private Set<Termino> terminosAlternativos = new HashSet<>();

	    // Conceptos inmediatamente superiores
	    private Set<ConceptoJuridico> terminosGenerales = new HashSet<>();

	    // Conceptos inmediatamente inferiores
	    private Set<ConceptoJuridico> terminosEspecificos = new HashSet<>();

	    // Conceptos relacionados, pero no jerárquicamente
	    private Set<ConceptoJuridico> terminosRelacionados = new HashSet<>();
	    
	    
}
