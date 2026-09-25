package io.demo.model;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class RAGSentencia extends RAGDocumento {

	private static final long serialVersionUID = 1L;

	@JsonProperty("fecha")
	private OffsetDateTime fecha;
	
	
	@JsonProperty("falloIdd")
	private String falloId;
	

	@JsonProperty("tribunal")
	private String tribunal;
	

	@JsonProperty("metadatos")
	private Map<String, String> metadatos;

	@JsonProperty("texto")
	private String texto;
	
	
	@JsonProperty("subject")
	private String subject;
	
	
	public RAGSentencia() {
		this.metadatos=new HashMap<String, String>();
	}
		
	public RAGSentencia(String id, String titulo, OffsetDateTime fecha, Map<String, String> metadatos, String texto) {
		super(id, titulo);
		this.fecha = fecha;
		this.metadatos = metadatos;
		this.texto = texto;
		
		
		if (this.metadatos == null )
			this.metadatos=new HashMap<String, String>();
		
		
		if (metadatos.get("tribunal")!=null)
			this.tribunal = metadatos.get("tribunal");
		else
			this.tribunal = "Corte Suprema de Justicia de Santa Fe";

	
		if (metadatos.get("materia")!=null)
			this.subject = metadatos.get("materia");

		

	}
	
	
	public void setFecha(OffsetDateTime fecha) {
		this.fecha = fecha;
	}
	

	public void setTribunal(String t) {
		this.tribunal = t;
	}

	

	public String getSubtitle() {

		StringBuilder str = new StringBuilder();


		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		
		if (fecha!=null) {
			str.append( fecha.format(formatter));
		}

		
		//if (metadatos.get("tribunal")!=null) {
		//	str.append( (str.length()>0?" - ":"") + metadatos.get("tribunal"));
		//}
		
		if (tribunal!=null && tribunal.length()>0)
			str.append( (str.length()>0?" - ":"") + this.tribunal);

		
		if (metadatos!=null && metadatos.get("cita")!=null) {
			str.append( (str.length()>0?" - ":"") + ( "Cita. "+ metadatos.get("cita")));
		}

		return str.toString();
	}
	
	
		
	/**
	 * 
	 * 
	 * Carátula: BONESSA, CRISTINA MONICA c/ PROVINCIA DE SANTA FE -RECURSO
	 * CONTENCIOSO ADMINISTRATIVO- s/ QUEJA POR DENEGACION DEL RECURSO DE
	 * INCONSTITUCIONALIDAD Fecha: 18/08/2026 Tribunal: Corte Suprema de Justicia
	 * Jueces: Jorge Camilo BACLINI - Daniel Aníbal ERBETTA - Roberto Héctor
	 * FALISTOCCO - Rafael Francisco GUTIERREZ - Eduardo Guillermo SPULER -
	 * 
	 * Fuente: Fuente Propia N° de expediente: Año de causa: N° de Tomo / Año: 2026
	 * N° de página de inicio: 0 N° de página de fin: 0 Resolución N°: 738 Cita:
	 * 738/26 N° de SAIJ: N° de CUIJ: 21 - 517447 - 0
	 * 
	 * 
	 */

}

/**
 * 
 * 
 * Cada una de esas preguntas debe mapear con el link del mismo numero
 * 
 * 1. - Precedente sobre responsabilidad del Estado provincial por inundaciones
 * - Precedente sobre requisitos que exige la Corte Suprema de Santa Fe para
 * anular una renuncia a reclamar daños por vicios de la voluntad - Precedente
 * donde se realice una interpretación de la ley Ley 12183
 * 
 * 2. - Precedente sobre quién es responsable si una pericia médica no se
 * realiza por falta de impulso procesal de la parte actora - Precedente sobre
 * si puede rechazarse una demanda laboral por incapacidad cuando no se acredita
 * la incapacidad con prueba médica - Precedente sobre si la Corte Suprema puede
 * revisar una sentencia que atribuye negligencia probatoria al trabajador
 * 
 * 3. - Precedente sobre posibilidad de reajuste de regulación de honorarios
 * profesionales - Precedente sobre si honorarios profesionales son obligación
 * de valor u obligación de dinero una vez que la regulación queda firme -
 * Precedente sobre posibilidad declaración de inconstitucionalidad de
 * prohibición de indexar para actualizar honorarios profesionales
 * 
 * 4. - Precedente sobre cálculo de reajuste de un haber jubilatorio -
 * Precedente sobre aplicación de doctrina de razonable proporcionalidad en las
 * jubilaciones de Santa Fe - Precedente sobre análisis de la movilidad
 * previsional
 * 
 * 5. - Precedente sobre límites de la doctrina de la cosa juzgada írrita en
 * materia penal - Precedente sobre alcance de garantía de intangibilidad de la
 * cosa juzgada en materia penal - Precedente sobre acuerdo de remediación
 * ambiental
 * 
 * 
 * 
 * https://bdj.justiciasantafe.gov.ar/index.php?pg=bus&m=busqueda&c=busqueda&a=get&id=53492
 * https://bdj.justiciasantafe.gov.ar/index.php?pg=bus&m=busqueda&c=busqueda&a=get&id=53584
 * https://bdj.justiciasantafe.gov.ar/index.php?pg=bus&m=busqueda&c=busqueda&a=get&id=53901
 * https://bdj.justiciasantafe.gov.ar/index.php?pg=bus&m=busqueda&c=busqueda&a=get&id=53774
 * https://bdj.justiciasantafe.gov.ar/index.php?pg=bus&m=busqueda&c=busqueda&a=get&id=53963
 * 
 */
