package io.demo.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Sumario extends RAGDocumento {

	private static final long serialVersionUID = 1L;

	@JsonProperty("texto")
	private String texto;

	@JsonProperty("fecha")
	private OffsetDateTime fecha;

	@JsonProperty("metadatos")
	private Map<String, String> metadatos;

	@JsonProperty("voces")
	private List<String> voces;

	@JsonProperty("fallo_id")
	private String fallo_id;

	public Sumario(String texto) {
		this.texto = texto;
	}

}

/**
 * 
 * CONSTITUCIONAL - PROCESAL
 * 
 * Tesauro > RECURSO DE INCONSTITUCIONALIDAD > QUEJA > INADMISIBILIDAD Tesauro >
 * RECURSO DE INCONSTITUCIONALIDAD > REQUISITOS PROPIOS > CUESTION NO
 * CONSTITUCIONAL > SENTENCIA SUFICIENTEMENTE FUNDADA Tesauro > SENTENCIA >
 * FUNDAMENTOS SUFICIENTES Tesauro > SENTENCIA SUFICIENTEMENTE FUNDADA Tesauro >
 * RECURSO DE INCONSTITUCIONALIDAD > QUEJA > MERA DISCREPANCIA Tesauro > ABOGADO
 * > HONORARIOS Tesauro > HONORARIOS Tesauro > HONORARIOS PROFESIONALES Tesauro
 * > HONORARIOS > REAJUSTE RECURSO DE INCONSTITUCIONALIDAD. QUEJA.
 * INADMISIBILIDAD. SENTENCIA SUFICIENTEMENTE FUNDADA. MERA DISCREPANCIA.
 * HONORARIOS PROFESIONALES. REAJUSTE.
 * 
 * Las tachas relacionadas con la pretendida aplicabilidad al caso del artículo
 * 8, inciso h), de la ley 6767 -en cuanto prevé la posibilidad de reajustar las
 * regulaciones practicadas en procesos contenciosos en que se realicen bienes,
 * atendiendo al resultado sobreviniente de dicha venta o realización-, tampoco
 * exhiben una elemental conexión con la realidad del caso, en tanto el
 * impugnante, con sus escasas alegaciones, falla en clarificar por qué la
 * tesitura adoptada por el A quo -que cuenta con respaldo doctrinario y
 * jurisprudencial- no conllevaría una lectura posible del asunto, y mucho menos
 * consigue demostrar, siquiera en el plano discursivo, que el incidente
 * concursal de marras pueda ser encauzado en el ámbito de aplicación de la
 * norma que invoca; con lo que sus cuestionamientos quedan así reducidos a un
 * simple intento del interesado en orden a imponer su propia postura en cuanto
 * a la solución que considera acertada, con base en su particular enfoque del
 * caso, mas sin llegar a delinear un posible vicio que amerite la
 * descalificación de la respuesta jurisdiccional atacada. - REFERENCIAS
 * NORMATIVAS: Ley 6767, artículo 8, inciso h. - DOCTRINA: García Solá, Marcela
 * y Eguren, María C., en “Código Procesal Civil y Comercial de la Provincia de
 * Santa Fe. Análisis doctrinario y jurisprudencial”, dir. Peyrano, coord.
 * Vázquez Ferreyra, Juris, 1999, T. 4-A, pág. 370.*
 */
