package io.demo.model.tesauro;

import io.demo.model.JsonObject;

public class RelacionTesauro extends JsonObject {

	private Long id;

    private ConceptoJuridico origen;

    private ConceptoJuridico destino;

    private TipoRelacion tipo;

    // opcional: información adicional
    private String nota;
    
}
