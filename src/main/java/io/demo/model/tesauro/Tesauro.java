package io.demo.model.tesauro;

import java.util.HashSet;
import java.util.Set;

import io.demo.model.JsonObject;

public class Tesauro extends JsonObject {

    private Long id;

    private String nombre;

    private String version;

    private String descripcion;

    private Set<ConceptoJuridico> conceptos = new HashSet<>();

    private Set<Termino> terminos = new HashSet<>();

    private Set<RelacionTesauro> relaciones = new HashSet<>();

    // Getters / setters

    
}
