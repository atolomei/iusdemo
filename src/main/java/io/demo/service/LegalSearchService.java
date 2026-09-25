package io.demo.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.demo.model.DemoObjectMapper;
import io.demo.model.Query;
import io.demo.model.RAGSentencia;
import io.demo.model.User;
import io.demo.service.rag.DocumentAnalysisResponse;
import io.demo.service.rag.KbeeRAGClient;
import io.demo.service.rag.RAGConverter;
import io.demo.service.rag.RagResponse;
import io.demo.util.Check;
import jakarta.annotation.PostConstruct;


import tools.jackson.databind.ObjectMapper;



@Service
public class LegalSearchService extends BaseService {

	static private io.demo.Logger logger = io.demo.Logger.getLogger(LegalSearchService.class.getName());

	
	/* Jackson 3 ObjectMapper */
	static final private  ObjectMapper jsonMapper = new DemoObjectMapper();
	
	
	private List<RAGSentencia> list;

	
	@Autowired
	DateTimeService dateService;
	
	@Autowired
	QueryHistoryService queryHistoryService;
	
	@Autowired
	DocumentAnalyzeCacheService documentAnalyzeCacheService;
	
	@Autowired
	KbeeRAGClient kbeeRAGClient;
	
	@Autowired
	QueryLogService queryLogService;
	
	
	public LegalSearchService(Settings settings, DateTimeService dateService, QueryHistoryService queryHistoryService, DocumentAnalyzeCacheService documentAnalyzeCacheService, KbeeRAGClient kbeeRAGClient, QueryLogService queryLogService) {
		super(settings);
		this.dateService=dateService;
		this.queryHistoryService=queryHistoryService;
		this.documentAnalyzeCacheService=documentAnalyzeCacheService;
		this.kbeeRAGClient=kbeeRAGClient;
		this.queryLogService=queryLogService;
	
	}

	
	public List<RAGSentencia> search(
			
			String text, 
			OffsetDateTime from,
			OffsetDateTime to,
			String subject,
			
			
			User user, 
			String sessionId) {
		
		boolean useCache = getSettings().isUseCacheQueries();
		
		return search(text, from, to,  subject, user, sessionId, useCache );
	}

	
	

	/**
	 * Executes a search.
	 *
	 * @param useCache whether the query cache is used (lookup and store). When
	 *                 {@code false} the search is always executed and the
	 *                 result is not stored in the cache.
	 */
	public List<RAGSentencia> search(	String text, 
			OffsetDateTime from,
			OffsetDateTime to,
			String subject,
			User user, 
			String sessionId, 
			boolean useCache) {
		return search(text, from, to, subject, user, sessionId, useCache, null, null, null);
	}

	/**
	 * Executes a search, logging the toolbar filter options selected by the user.
	 * The {@link io.demo.results.ReasoningEffortOption} (topK + key) is forwarded to
	 * the Kbee RAG Server API.
	 */
	public List<RAGSentencia> search(	
			String text, 
			OffsetDateTime from,
			OffsetDateTime to,
			String subject,
			User user, 
			String sessionId, 
			boolean useCache,
			io.demo.results.DateRange dateRange,
			io.demo.results.SubjectOption subjectOption,
			io.demo.results.ReasoningEffortOption reasoningEffortOption) {

		// record the query in the user's history (session-scoped). When called
		// from a non-web thread (e.g. the regression test runner) there is no
		// active HTTP session, so the session-scoped bean is not available and
		// the history is simply skipped
		try {
			getQueryHistoryService().record(text);
		} catch (org.springframework.beans.factory.support.ScopeNotActiveException e) {
			logger.debug("no active session -> query history not recorded");
		}

		if (!useCache)
			return executeSearch(text, from, to, subject, user, sessionId, dateRange, subjectOption, reasoningEffortOption);

		// if the query is in the cache -> return the cached result. The cache
		// key is the hash of text + date range + subject + reasoning effort,
		// so a change in any of those options is a cache miss
		Optional<List<RAGSentencia>> cached = getQueryLogService().get(text, dateRange, subjectOption, reasoningEffortOption);

		if (cached.isPresent()) {
			return cached.get();
		}

		// otherwise perform the query and store the result in the cache
		List<RAGSentencia> result = executeSearch(text, from, to, subject, user, sessionId, dateRange, subjectOption, reasoningEffortOption);
		getQueryLogService().put(text, dateRange, subjectOption, reasoningEffortOption, result);
		
		
		return result;
	}

	
	
	
	
	
	
	
	//public List<RAGSentencia> search(String text, User user, String sessionId) {
	//	return search(text, user, sessionId, true);
	//}

	/**
	 * Calls the Kbee RAG Server document analysis endpoint for the given
	 * document and query, and returns the answer.
	 *
	 * @param ragDocumentId id of the document (segment) in the RAG index
	 * @param query         the question to answer about the document
	 * @return the answer of the {@link io.demo.service.rag.DocumentAnalysisResponse}
	 */
	public String analyzeDocument(String ragDocumentId,Query query,  User user, String sessionId) {
		return analyzeDocumentResponse(ragDocumentId, query, user, sessionId).answer();
	}

	/**
	 * Same as {@link #analyzeDocument(String, String)} but returns the full
	 * {@link DocumentAnalysisResponse} (cached).
	 */
	public DocumentAnalysisResponse analyzeDocumentResponse(String ragDocumentId,Query query, User user, String sessionId) {

		Check.requireNonNull(ragDocumentId, "ragDocumentId");
		Check.requireNonNull(query, "query is null");
		Check.requireNonNull(user, "user is null");
		Check.requireNonNull(sessionId, "sessionId is null");
		
		
		
		// if the analysis is in the cache -> return the cached answer
		Optional<DocumentAnalysisResponse> cached = getDocumentAnalyzeCacheService().get(ragDocumentId, query.getId().toString());
		
		if (cached.isPresent() && getSettings().isUseCacheDocumentAnalyzeQueries())
			return cached.get();

		// otherwise call the RAG server and store the response in the cache
		long startTime = System.currentTimeMillis();
		DocumentAnalysisResponse response = kbeeRAGClient.analyzeDocument(ragDocumentId, query);
		long durationMillisecs = System.currentTimeMillis() - startTime;
		getDocumentAnalyzeCacheService().put(ragDocumentId, query.getId().toString(), response, durationMillisecs, user, sessionId);
		return response;
	}

	/**
	 * Returns the general analysis of the query.
	 * <p>
	 * If the {@link Query} already has a saved analysis and the query cache is
	 * enabled, the saved value is returned. Otherwise the Kbee RAG Server
	 * {@code queryanalysis} endpoint is called with the server's query id, and
	 * the analysis is saved with the query for following requests.
	 * </p>
	 *
	 * @param query the query (must have a server id)
	 * @param llm   the llm to use; null -> the same llm used for the query
	 * @return the query with the analysis fields set
	 */
	public Query queryAnalysis(Query query, String llm) {

		Check.requireNonNull(query, "query is null");

		// saved value, if present and the query cache is enabled
		if (query.getAnalysisText() != null && getSettings().isUseCacheQueries())
			return query;

		if (query.getServerId()==null) {
			logger.error("query has no server id -> " + query.toString());
			return query;
		}

		// by default use the same llm as the query
		String analysisLlm = (llm != null && !llm.isBlank()) ? llm : getSettings().getRagLlm();

		io.demo.service.rag.QueryAnalysis analysis = kbeeRAGClient.queryAnalysis(query.getServerId(), analysisLlm);

		query.setAnalysisText(analysis.analysis());
		query.setAnalysisLlm(analysis.llm());
		query.setAnalysisDate(analysis.dateCreated() != null ? analysis.dateCreated() : OffsetDateTime.now());

		// persist the analysis with the query
		if (query.getId() != null)
			getQueryLogService().getQueryDBService().save(query);

		return query;
	}

	
	public QueryHistoryService getQueryHistoryService() {
		return this.queryHistoryService;
	}

	public DocumentAnalyzeCacheService getDocumentAnalyzeCacheService() {
		return this.documentAnalyzeCacheService;
	}

	
	@PostConstruct
	public void init() {

		list = new ArrayList<>();

		Locale.setDefault(Locale.forLanguageTag("es"));
		
		
		// 1 -----------------------------------------------------
		
		
		
		/**
		 * 
		 * 
		 * 
		 * 
		 * Trata directamente tres cuestiones:
- La ausencia de prueba de la patología laboral y del grado de incapacidad.
- La falta de producción de la prueba pericial médica y psicológica, que era la prueba destinada a acreditar esos extremos.
- La consecuencia procesal de esa carencia probatoria: la confirmación del rechazo de la demanda.
El punto más importante para un sistema RAG es distinguir entre ratio/fundamento relevante y circunstancias accesorias. En este caso, la Corte señala expresamente que la sentencia de grado había rechazado la demanda debido a la “absoluta carencia de prueba” sobre la patología laboral y el grado de incapacidad, y que la Cámara confirmó esa conclusión.
Además, el fallo agrega circunstancias que fortalecen esa conclusión: la actora no había alegado originalmente una incapacidad laboral permanente, no había acompañado certificado médico y la única documentación relevante indicaba incluso ausencia de incapacidad.


		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		
		
		
		/*
		 * 
		 * 
		 * 
		 * “...en donde evidenció que los cuestionamientos [...] no alcanzaban a conmover lo decidido por el Juez de grado cuando señaló la absoluta carencia de prueba ninguna que demostrara la supuesta patología laboral que se denunciaba ni tampoco el grado de incapacidad.”

Este es probablemente el fragmento principal, porque conecta directamente la falta de prueba con el rechazo de la demanda.
Otro fragmento particularmente fuerte:
“...la Cámara añadió, como circunstancia llamativa, que en su demanda la accionante no había referido a incapacidad laboral permanente ninguna; que tampoco había acompañado ningún certificado médico que pudiera brindar algún aval a sus alegaciones; y que, finalmente, la única documentación trascendente acompañada no consistía más que en un formulario en donde, para más, se indicaba la ausencia de incapacidad.”


		 */
		
		{
			Map<String, String> map = new HashMap<String, String>();

			map.put("tribunal", "Corte Suprema de Justicia");
			map.put("jueces", "Jorge Camilo BACLINI - Daniel Aníbal ERBETTA - Roberto Héctor FALISTOCCO - Rafael Francisco GUTIERREZ - Eduardo Guillermo SPULER");
			map.put("fecha", "18/08/2026");
			map.put("cita", "738/26");
			
			map.put("fuente",
					"Fuente Propia N° de expediente: Año de causa: N° de Tomo / Año: 2026\n" + "	 * N° de página de inicio: 0 N° de página de fin: 0 Resolución N°: 738 Cita:\n" + "	 * 738/26 N° de SAIJ: N° de CUIJ: 21 - 517447 - 0");

			RAGSentencia s1= new RAGSentencia("1", "NUÑEZ, ALFREDO c/ LA SEGUNDA ART S.A. -SENTENCIA ACCIDENTE Y/O ENFERMEDAD TRABAJO- s/ QUEJA POR DENEGACION DEL RECURSO DE INCONSTITUCIONALIDAD",
					getDateTimeService().parseOffsetDateTime("18/08/2026"), map,
					"AUTOS Y SENTENCIAS NRO. 738 AÑO 2026.\n" + "\n" + "Provincia de Santa Fe, 18 de agosto del año 2026.\n"
							+ "VISTA: La queja por denegación del recurso de inconstitucionalidad planteado por la demandada contra la resolución de fecha 8 de abril del año 2024, dictada por la Cámara de lo Contencioso Administrativo N° 1 en autos \"BONESSA, CRISTINA MÓNICA contra PROVINCIA DE SANTA FE -RECURSO CONTENCIOSO ADMINISTRATIVO- (CUIJ 21-17477734-6)\" (Expte. C.S.J. CUIJ N°: 21-00517447-0); y,\n"
							);
				
							
							s1.setAnalysis( "Trata directamente tres cuestiones:<br/>"
									+"- La ausencia de prueba de la patología laboral y del grado de incapacidad.<br/>"
									+"- La falta de producción de la prueba pericial médica y psicológica, que era la prueba destinada a acreditar esos extremos.<br/>"
									+"- La consecuencia procesal de esa carencia probatoria: la confirmación del rechazo de la demanda.<br/>"
									+ "la Corte señala expresamente que la sentencia de grado había rechazado la demanda debido a la "
									+ "<span class=\"quote\">'absoluta carencia de prueba”</span> "
									+ "sobre la patología laboral y el grado de incapacidad, y que la Cámara confirmó esa conclusión.<br/>"
									+"Además, el fallo agrega circunstancias que fortalecen esa conclusión: la actora no había alegado originalmente una incapacidad laboral permanente,"
									+ " no había acompañado certificado médico y la única documentación relevante indicaba incluso ausencia de incapacidad.");
							
							
							
							List<String> quotes = new ArrayList<>();
							quotes.add(" <i>\"..en donde evidenció que los cuestionamientos [...] no alcanzaban a conmover lo decidido por el Juez de grado cuando señaló la absoluta carencia de prueba ninguna que demostrara la supuesta patología laboral que se denunciaba ni tampoco el grado de incapacidad.</i>");
							quotes.add("<i>\"..la Cámara añadió, como circunstancia llamativa, que en su demanda la accionante no había referido a incapacidad laboral permanente ninguna; que tampoco había acompañado ningún certificado médico que pudiera brindar algún aval a sus alegaciones; y que, finalmente, la única documentación trascendente acompañada no consistía más que en un formulario en donde, para más, se indicaba la ausencia de incapacidad.</i>");

					
							List<String> fallos = new ArrayList<>();
							fallos.add("<i>\"Londero\", A. y S. T. 271, pág. 151</i>");
							fallos.add("<i>\"Constructora del Litoral S.A.\", A. y S. T. 290, pág. 229</i>");

							List<String> normas = new ArrayList<>();
							normas.add("<i>Artículo 95 de la Constitución provincial</i>");
							normas.add("<i>Ley 7055, artículo 1, incisos 2° y 3°</i>");

							s1.setQuotes(quotes);
							s1.setCitasFallos(fallos);
							s1.setCitasNormas(normas);
							list.add(s1);
		}
		
		
		
		
		// 2 -----------------------------------------------------

		
		{

			Map<String, String> map = new HashMap<String, String>();

			map.put("tribunal", "Corte Suprema de Justicia");
			map.put("jueces", "J Daniel Aníbal ERBETTA - Roberto Héctor FALISTOCCO - María Angélica GASTALDI - Rafael Francisco GUTIERREZ - Eduardo Guillermo SPULER -");
			map.put("fecha", "06/03/2025");
			map.put("cita", "738/26");
			map.put("fuente",
					"Fuente Propia N° de expediente: Año de causa: N° de Tomo / Año: 2026\n" + "	 * N° de página de inicio: 0 N° de página de fin: 0 Resolución N°: 738 Cita:\n" + "	 * 738/26 N° de SAIJ: N° de CUIJ: 21 - 517447 - 0");
			
			
			map.put("N° de Tomo / Año",
					"2025");
			
			RAGSentencia s2 = new RAGSentencia("2", "MALDONADO, JORGE ALBERTO Y OTROS c/ PROVINCIA DE SANTA FE -DAÑOS Y PERJUICIOS- s/ RECURSO DE INCONSTITUCIONALIDAD (CONCEDIDO POR LA CAMARA)",
					getDateTimeService().parseOffsetDateTime("06/03/2025"), 
					map,
					 
					"T. 2025, SENTENCIA NRO. 450"
					+ "	Provincia de Santa Fe, 29 de julio del año 2025."
					+ "VISTA: La queja por denegación del recurso de inconstitucionalidad interpuesto por el abogado Norberto Francisco José Berlanga contra el auto número 233 de fecha 15 de octubre de 2024, dictado por la Sala Primera -integrada- de la Cámara de Apelación en lo Civil y Comercial de la ciudad de Santa Fe, en autos 'VERONESE, CLAUDIA contra RECORD PUBLICISTAS S.R.L. Y "
					+ "OTROS -INCID DE INOP DE INSC BIEN FAM (CUIJ 21-00834525-9)' (Expte. C.S.J. CUIJ Nº: 21-00516437-8); y,"
					+ "CONSIDERANDO:"
					+ "1. Mediante resolución 233 del 15 de octubre de 2024, la Sala Primera integrada de la Cámara de Apelación en lo Civil y Comercial de Santa Fe rechazó el recurso de reposición deducido por el doctor Berlanga contra la providencia de fecha 06.08.2024 dictada por el Vocal de trámite -quien, a su turno, había desestimado la petición del letrado orientada al reajuste de los honorarios regulados por la actuación profesional desarrollada en la segunda instancia de un incidente concursal-."
					+ "Contra tal pronunciamiento interpone el curial recurso de inconstitucionalidad, con invocación de las causales previstas en el artículo 1 -incisos 2° y 3°- de la ley 7055, tachándolo de arbitrario, contrario a la Constitución y a la ley arancelaria, y carente de motivación suficiente."
					+ "En fundamentación del recurso impetrado, reseña que los presentes se originaron a partir de su solicitud, formulada ante el Tribunal de Alzada, orientada al reajuste de los honorarios regulados por su labor profesional desplegada en el marco de un incidente concursal en segunda instancia, el cual tenía por objeto -recuerda- la declaración de inoponibilidad de la inscripción como bien de familia de un inmueble de la fallida, en orden a posibilitar su ulterior subasta en el trámite de quiebra liquidativa; remarca que aquella petición se sustentaba en lo establecido en los artículos 8, inciso h), y 32 de la ley 6737 -y sus modificatorias-"
					+ "Destaca que los honorarios en cuestión habían sido regulados mediante auto de fecha 19.08.2008 en 30,83 jus, equivalentes en aquel entonces a $4.523,08, determinándose el interés moratorio a una tasa del 12% anual; entiende que la denegación del reajuste peticionado prescinde del actual contexto inflacionario, a la vez que se aparta de lo normado en la ley arancelaria acerca del valor actualizado del jus, careciendo asimismo de toda conexión con el precio de subasta del inmueble involucrado en el proceso incidental de marras; todo ello -prosigue- con grave afectación de sus derechos fundamentales de propiedad y justa retribución, proporcional al esfuerzo desplegado y a los intereses económicos comprometidos."
					);
		
		
			s2.setAnalysis("analisis sentencia 2");
			list.add(s2);

			
		}

		
		
		// 3 -----------------------------------------------------
		
		
		{

			Map<String, String> map = new HashMap<String, String>();

			map.put("tribunal", "Corte Suprema de Justicia");
			map.put("jueces", "J  Daniel Aníbal ERBETTA - Roberto Héctor FALISTOCCO - Eduardo Guillermo SPULER - Rubén Luis WEDER - Margarita Elsa ZABALZA -");
			map.put("fecha", "29/07/2025");
			map.put("cita", "738/26");
			
			map.put("fuente",
					"Fuente Propia N° de expediente: Año de causa: N° de Tomo / Año: 2026\n" + "	 * N° de página de inicio: 0 N° de página de fin: 0 Resolución N°: 738 Cita:\n" + "	 * 738/26 N° de SAIJ: N° de CUIJ: 21 - 517447 - 0");
			
			
			map.put("N° de Tomo / Año",
					"2025");
			
			RAGSentencia s3 = new RAGSentencia("3", "VERONESE, CLAUDIA c/ RECORD PUBLICISTAS S.R.L. Y OTROS -INCIDENTE DE INOPONIBILIDAD DE INSCRIPCION COMO BIEN DE FAMILIA- s/ QUEJA POR DENEGACION DEL RECURSO DE INCONSTITUCIONALIDAD",
					getDateTimeService().parseOffsetDateTime("29/07/2025"), 
					map,
					 
					"T. 2025, SENTENCIA NRO. 52\n"
					+ "\n"
					+ "En la Provincia de Santa Fe, a los seis días del mes de marzo del año dos mil veinticinco, los señores Ministros de la Corte Suprema de Justicia de la Provincia, doctores Daniel Aníbal Erbetta, María Angélica Gastaldi, Rafael Francisco Gutiérrez y Eduardo Guillermo Spuler, con la Presidencia de su titular doctor Roberto Héctor Falistocco, acordaron dictar sentencia en los autos \"MALDONADO, JORGE ALBERTO Y OTROS contra PROVINCIA DE SANTA FE -DAÑOS Y PERJUICIOS- (CUIJ N° 21-12061123-9) sobre RECURSO DE INCONSTITUCIONALIDAD (CONCEDIDO POR LA CÁMARA)\" (Expte. C.S.J. CUIJ N°: 21-12061123-9). Se decidió someter a decisión las siguientes cuestiones: PRIMERA: ¿es admisible el recurso interpuesto?; SEGUNDA: en su caso, ¿es procedente?; y TERCERA: en consecuencia, ¿qué resolución corresponde dictar? Asimismo, se emitieron los votos en el orden que realizaron el estudio de la causa, o sea, doctores Spuler, Gutiérrez, Falistocco, Gastaldi y Erbetta.\n"
					+ "A la primera cuestión -¿es admisible el recurso interpuesto?- el señor Ministro doctor Spuler dijo:\n"
					+ "Mediante resolución N° 62 de fecha 5 de junio de 2024 (f. 1165) la Sala Segunda de la Cámara de Apelación en lo Civil y Comercial de Santa Fe declaró admisible el recurso de inconstitucionalidad deducido por la Provincia de Santa Fe contra la sentencia N° 72 emitida por dicho Tribunal en fecha 3 de mayo de 2023 (fs. 1099/1118).\n"
					+ "El nuevo examen de admisibilidad -que corresponde a esta Corte efectuar por imperio del artículo 11 de la ley 7055, con los principales a la vista-, me conduce a ratificar esa conclusión, de conformidad con lo dictaminado por el señor Procurador General Subrogante (fs. 1176).\n"
					+ "Voto, pues, por la afirmativa.\n"
					+ "A la misma cuestión, el señor Ministro doctor Gutiérrez, el señor Presidente doctor Falistocco, la señora Ministra doctora Gastaldi y el señor Ministro doctor Erbetta expresaron idénticos fundamentos a los vertidos por el señor Ministro doctor Spuler y votaron en igual sentido.\n"
					+ "A la segunda cuestión -en su caso, ¿es procedente?-, el señor Ministro doctor Spuler dijo:\n"
					+ "1. En la presente causa los hechos pueden reseñarse de la siguiente manera:\n"
					+ "1.1. Mediante sentencia emitida el 28 de julio de 2020, el Tribunal Colegiado de Responsabilidad Extracontractual Nº 1 del Distrito Judicial N° 1 hizo lugar parcialmente a la demanda interpuesta por Jorge Alberto Maldonado y María Belén Maldonado, por sí y en representación de sus hijos menores de edad contra la Provincia de Santa Fe. En efecto, condenó a la demandada al resarcimiento de la totalidad de los daños sufridos por los accionantes como consecuencia del desborde del río Salado ocurrido en abril de 2003 en la ciudad de Santa Fe. Por su parte, rechazó la pretendida responsabilidad de la Provincia por el fallecimiento del hijo de los actores.\n"
					+ "Para así decidir, el Tribunal Colegiado descartó, en primer lugar, la falta de legitimación pasiva planteada por la accionada por haber recibido los actores el pago de la \"ayuda extraordinaria\" establecida mediante ley provincial 12183, modificada por ley 12259.\n"
					+ "Para ello, declaró la inconstitucionalidad de la normativa provincial de referencia, por considerar que al establecer como condición la renuncia a iniciar (o continuar) acciones resarcitorias contra el Estado para percibir una ayuda económica, constituye un valladar al acceso a la justicia a personas en situación de vulnerabilidad y viola el principio de reparación intgral.\n"
					+ "A continuación el Tribunal, reseñó la prueba rendida en la causa y los precedentes \"Micucci\" y \"Doumani\", y consideró que no había motivos para apartarse de las conclusiones vertidas en dichos pronunciamientos. Y a partir de ello, sostuvo que \"en el contexto de responsabilidad objetiva en el que jurídicamente se encuadra el caso, el Estado provincial no ha llogrado acreditar la eximente de responsabilidad invocada, es decir, el caso fortuito, que en el supuesto concreto requería la demostración de que, aun desplegando la actividad compatible con una prestación regular del servicio, era inevitable que el inmueble en que habitaban los accionantes, sito en calle Aguado 2205 de esta ciudad sufra las consecuencias de la catástrofe hídrica\".\n"
					+ "Por su parte, los Sentenciantes rechazaron la pretendida responsabilidad de la Provincia por el fallecimiento del hijo de los accionantes, J. M.. Al respecto, con apoyo en el informe pericial médico incorporado a la causa, como así también en las declaraciones testimoniales de la doctora Bortoluzzi, Bagnasco, Zorzón y del doctor Rosso, entendieron que la parte actora no logró acreditar que el deceso del niño se encuentre causalmente vinculado con el accionar de la demandada en la emergencia hídrica ocurrida en el mes de abril del año 2003.\n"
					+ "Finalmente, con relación a los rubros indemnizatorios demandados, el Colegiado concedió por las consecuencias patrimoniales la suma de $185541,51 por el rubro \"gastos de reparación del inmueble\"; $20000 por la pérdida de bienes muebles; $390000 por el rubro \"desvalorización del inmueble\"; y por las consecuencias no patrimoniales, la suma de $80000 por cada uno de los demandantes.\n"
					+ "1.2. Contra la sentencia del Tribunal, se alzaron ambas partes mediante la interposición del recurso de apelación extraordinario (art. 42, ley 10160), los cuales fueron denegados por el Cuerpo mediante resolución del 13 de noviembre de 2020.\n"
					+ "1.3. Disconformes con este último pronunciamiento, tanto los actores como la demandada opusieron la pertinente queja ante la Sala Segunda de la Cámara de Apelación en lo Civil y Comercial de la ciudad de Santa Fe, que los juzgó mal denegados y, como consecuencia, los concedió.\n"
					+ "1.4. Mediante resolución de fecha 3 de mayo del 2023 la Sala Segunda de la Cámara de Apelación en lo Civil y Comercial -integrada- resolvió: rechazar el recurso de apelación extraordinaria interpuesto por la demandada con costas a su cargo; y admitir parcialmente el recurso de apelación extraordinaria articulado por la parte actora. Como consecuencia, casó el pronunciamiento impugnado, y fijó una tasa de interés del 8% anual para los rubros \"gastos de reparación del inmueble\" y \"desvalorización del inmueble\" desde la fecha del hecho y hasta el 4.4.2018, y confirmó la sentencia en todo lo demás. Impuso las costas en un 50% a la parte actora y en un 50% a la demandada.\n"
					+ "2. Contra este último pronunciamiento, la Provincia de Santa Fe interpuso recurso de inconstitucionalidad con fundamento en el artículo 1, incisos 2 y 3, de la ley 7055.\n"
					+ "2. 1. La recurrente sostiene que la decisión de la Sala es arbitraria por adolecer de deficiencias graves de motivación, omisión de tratamiento de agravios centrales y desconocimiento del derecho aplicable.\n"
					+ "Esgrime que las conclusiones de la Sala sobre la falta de servicio eluden el aspecto central del debate que fue propuesto en la impugnación: cómo se conformó la relación de causalidad estrictamente necesaria para imputar total o parcialmente responsabilidad, cuando la única prueba rendida estimó que el fenómeno era excepcional, con una posibilidad de ocurrencia de solo un 0,2% y un nivel de recurrencia de 800 años.\n"
					+ "Refiere que \"ese era singularmente el sentido de acuse de fuerza mayor que obstaba que la Provincia pudiese diseñar un sistema de defensa eficaz ante la magnitud del fenómeno e, incluso, formular una alerta más temprana de aquella que emitió\".\n"
					+ "Dice que los Magistrados omitieron que en el caso intervinieron hechos de la naturaleza y antrópicos, imprevisibles e inevitables, de probada incidencia causal en el resultado dañoso, de los cuales prescindió el Tribunal Colegiado para establecer los alcances de la responsabilidad y que, en cambio, sí se tuvieron en cuenta en sede penal. Cita lo expuesto por esta Corte en la causa \"Mazzini\" acerca de la importancia de indagar sobre el nexo causal y los factores que habrían intercedido sobre la causación de los daños.\n"
					+ "Por su parte, plantea que es arbitrario el razonamiento por el cual se desestimó la falta de acción de la parte actora por aceptar las reparaciones que otorgó la Administración y formular su renuncia a reclamar.\n"
					+ "Al respecto, sostiene que el Tribunal se desentendió de las alegaciones de la Provincia con el argumento del estado de necesidad, cuando la renuncia a la acción se justificaba por razones de interés general para mantener indemne el erario público y demandó una notable inversión patrimonial destinada a los damnificados del fenómeno hídrico.\n"
					+ "Aduce que la Cámara no se hizo cargo de la invocación del precedente \"Cabrera\" de la Corte Nacional, e insistió con la figura del estado de necesidad, que no puede aplicarse cuando el régimen instituido por la ley provincial que era de caráter general y se componía de pautas generales para acordar la asistencia económica que variaba de acuerdo al nivel de ingreso de aguas en las viviendas y a la pérdida de los bienes.\n"
					+ "Añade que la Alzada omitió que sus agravios se basaron en cuestionar que la sentencia desconocía que la hipótesis de lesión por aprovechamiento del estado de necesidad, importa un vicio que solo autoriza a demandar la nulidad del acto tanto en la redacción originaria del Código Civil como en el actual artículo 332 del Código Civil y Comercial, lo que no hizo la parte actora y ya se encuentra prescripto; que el principio general es el de la validez y conservación de los negocios jurídicos; y que las circunstanicas fácticas que pueden dar lugar a su aplicación deben apreciarse de manera rigurosa, lo que se encuentra remarcado por la sentencia de esta Corte en \"Dietz\"; que la ley 12183 tuvo en consideración una suerte de reparación tarifada que ha sido entendida como propia y necesaria para resarcir catástrofes humanas o naturales; y que el criterio negativo hacia la admisión de la renuncia quiebra la seguridad jurídica porque en base con tales pautas laxas prácticamente cualquier transacción sería revisable en cualquier tiempo.\n"
					+ "Resalta que el argumento de la falta de proporcionalidad entre el beneficio y el daño es falso, pues si al valor abonado por la Administración se le aplica la evolución del índice de precios al consumidor hasta la fecha de la sentencia, se obtiene un resultado que deja sin fundamento las afirmaciones de irrazonabilidad de la Sala.\n"
					+ "Alega que tampoco se ponderó el argumento relativo a que la renuncia de demandabilidad fue aceptada como válida por la jurisprudencia local y federal en supuestos similares en los que el Estado decidió asistir a sujetos damnificados por fenómenos o hechos generales.\n"
					+ "Por último, invoca la existencia de gravedad institucional por considerar que la cuestión excede el mero interés particular e involucra los intereses de la comunidad en su conjunto.\n"
					+ "3. Con base en el relato efectuado precedentemente, se adelanta que merecen favorable acogida los agravios vinculados con la arbitrariedad del pronunciamiento impugnado por rechazar la falta de acción a pesar de la renuncia expresa formulada por los ahora actores.\n"
					+ "En efecto, teniendo en cuenta que se halla fuera de toda discusión que los accionantes recibieron el pago de la \"ayuda extraordinaria\" contemplada en el régimen reparatorio especial establecido por ley 12183 (modif. por ley 12259), la cuestión planteada es sustancialmente análoga a la considerada y resuelta por este Tribunal en los precedentes \"Villa\" y \"Ulrich\" (A. y S. nro. 82 y nro. 83, año 2024), a cuyos fundamentos se remite en lo pertinente, y se dan aquí por reproducidos por razones de economía procesal.\n"
					+ "Sentada en dichos precedentes la disponibilidad de los derechos patrimoniales en juego, resta aquí añadir que en aquellos fallos esta Corte descartó que la vulnerabilidad, urgencia o necesidad fueran fundamentos suficientes para declarar la inconstitucionalidad del artículo 7 de la ley 12183, y expresó que tales circunstancias podrían a lo sumo ser ponderadas por los Sentenciantes, eventualmente, a fin de examinar la existencia de algún vicio de la voluntad nulificante de los respectivos actos jurídicos de renuncia. Se hizo especial énfasis en que una solución de ese tipo debía basarse en esfuerzos argumentales y probatorios específicos que en tal sentido hubiese desplegado el accionante.\n"
					+ "Pues bien, esto último no ocurre en el caso, dado que la Sala, para confirmar el rechazo de la falta de legitimación opuesta por la demandada, en vez de apoyarse en una pretensión específica de nulidad por parte del actor -con la consiguiente argumentación y prueba concreta al respecto- se sustentó en que el ingreso del agua a la vivienda del señor Maldonado revelaba que la aceptación de la ayuda en esa situación se había llevado a cabo sin discernimiento.\n"
					+ "Esta respuesta pasa por alto que el dramático y lamentable acontecimiento, sin dudas capaz de generar desesperación, angustia y un sin número de sentimientos desagradables, fue vivenciado por una gran cantidad de ciudadanos santafesinos, algunos de los cuales -como el actor- aceptaron el dinero de manera anticipada y otros, pese a encontrarse frente al mismo escenario, no la admitieron y optaron por la vía judicial para reclamar el resarcimiento integral de los daños sufridos.\n"
					+ "En este contexto, la indemnización anticipada prevista por la normativa de emergencia significó un esfuerzo de toda la población -porque naturalmente se solventó con el erario público- por asumir un importante desembolso de fondos en un momento crítico y aun antes de estar definida la responsabilidad del Estado Provincial en el evento. Esta posibilidad, como consecuencia de una decisión legislativa, requería a cambio la renuncia a una demanda futura.\n"
					+ "Por esa razón, solamente la específica pretensión de nulidad del acto jurídico de aceptación, acompañada de prueba contundente que demuestre que una situación peculiar del accionante lo dejó sin más opción que recibir el dinero en ese momento, podría haber justificado su ineficacia en el caso concreto y, con ello, un trato diferente al que tuvieron el resto de los habitantes que también sufrieron las consecuencias devastadoras del ingreso del agua a sus hogares.\n"
					+ "En consonancia con los argumentos expuestos, cabe concluir que la sentencia atacada no brindó en este punto razones suficientes en orden a rechazar el recurso de apelación extraordinaria interpuesto por parte de la accionada y, por ende, resulta descalificable por su falta de motivación en los términos del artículo 95 de la Constitución provincial.\n"
					+ "Voto, pues, por la afirmativa.\n"
					+ "A la misma cuestión, el señor Ministro doctor Gutiérrez, el señor Presidente doctor Falistocco, la señora Ministra doctora Gastaldi y el señor Ministro doctor Erbetta expresaron idénticos fundamentos a los expuestos por el señor Ministro doctor Spuler y votaron en igual sentido.\n"
					+ "A la tercera cuestión -en consecuencia ¿qué resolución corresponde adoptar?- el señor Ministro doctor Spuler dijo:\n"
					+ "Atento al resultado obtenido al tratar la cuestión anterior corresponde declarar procedente el recurso de inconstitucionalidad concedido y, en consecuencia, revocar con ese alcance la sentencia atacada. Ordenar la devolución de los autos al tribunal de origen para que se pronuncie nuevamente conforme a las pautas trazadas por la Corte. Costas del presente a la vencida (art. 12, ley 7055).\n"
					+ "Así voto.\n"
					+ "A la misma cuestión, el señor Ministro doctor Gutiérrez, el señor Presidente doctor Falistocco, la señora Ministra doctora Gastaldi y el señor Ministro doctor Erbetta dijeron que la resolución que correspondía adoptar era la propuesta por el señor Ministro doctor Spuler y así votaron.\n"
					+ "En mérito a los fundamentos del acuerdo que antecede la Corte Suprema de Justicia de la Provincia RESOLVIÓ: Declarar procedente el recurso de inconstitucionalidad concedido y, en consecuencia, revocar con ese alcance la sentencia atacada. Ordenar la devolución de los autos al tribunal de origen para que se pronuncie nuevamente conforme a las pautas trazadas por la Corte. Costas del presente a la vencida.\n"
					+ "Registrarlo y hacerlo saber.\n"
					+ "Con lo que concluyó el acto, firmando el señor Presidente y los señores Ministros, de lo que doy fe.\n"
					+ "\n"
					+ "FDO. DIGITALMENTE: FALISTOCCO - ERBETTA - GASTALDI - GUTIÉRREZ - SPULER - PORTILLA (SECRETARIA).\n"
					+ "\n"
					+ "\n"
					+ "Tribunal de origen: Cámara de Apelación Civil y Comercial, Sala III de Rosario.\n"
					+ "Tribunal que intervino con anterioridad: Juzgado de Primera Instancia de Distrito en lo Civil y Comercial de la 12 Nominación de Rosario."
					
					);

			s3.setAnalysis("analisis sentencia 3");
			list.add(s3);

			
		}
		
		
		

		// 4 -----------------------------------------------------

		
		{

			Map<String, String> map = new HashMap<String, String>();

			map.put("tribunal", "Corte Suprema de Justicia");
			map.put("jueces", "J Daniel Aníbal ERBETTA - Roberto Héctor FALISTOCCO - María Angélica GASTALDI - Rafael Francisco GUTIERREZ - Eduardo Guillermo SPULER -");
			map.put("fecha", "06/03/2025");
			map.put("cita", "738/26");
			
			map.put("fuente",
					"Fuente Propia N° de expediente: Año de causa: N° de Tomo / Año: 2026\n" + "	 * N° de página de inicio: 0 N° de página de fin: 0 Resolución N°: 738 Cita:\n" + "	 * 738/26 N° de SAIJ: N° de CUIJ: 21 - 517447 - 0");
			
			
			map.put("N° de Tomo / Año",
					"2025");
			
			RAGSentencia s4 =  new RAGSentencia("4", "SBRISSA, DANIEL ANTONIO c/ PROVINCIA DE SANTA FE -RECURSO CONTENCIOSO ADMINISTRATIVO- s/ RECURSO DE INCONSTITUCIONALIDAD (PARCIALMENTE CONCEDIDO POR LA CAMARA)",
					getDateTimeService().parseOffsetDateTime("11/06/2025"), 
					map,
					"En la Provincia de Santa Fe, a los once días del mes de junio del año dos mil veinticinco, los señores Ministros de la Corte Suprema de Justicia de la Provincia, "
					+ "doctores Daniel Aníbal Erbetta, Rafael Francisco Gutiérrez, Eduardo Guillermo Spuler y Margarita Elsa Zabalza, con la presidencia de su titular doctor Roberto Héctor Falistocco, acordaron dictar sentencia en los autos caratulados "
					+ "'SBRISSA, Daniel Antonio contra PROVINCIA DE SANTA FE -RCA- (CUIJ 21-17455462-2) sobre RECURSO DE INCONSTITUCIONALIDAD (PARCIALMENTE CONCEDIDO POR LA CÁMARA)' "
					+ "(Expte. C.S.J. CUIJ N° 21-17455462-2). Se resolvió someter a decisión las siguientes cuestiones: PRIMERA: ¿es admisible el recurso interpuesto? SEGUNDA: en su caso, ¿es procedente? "
					+ "TERCERA: en consecuencia, ¿qué resolución corresponde dictar? Asimismo, se emitieron los votos en el orden que realizaron el estudio de la causa, o sea doctores Gutiérrez, Spuler, Erbetta, Falistocco y Zabalza.");
					
	
			 
			
		}
		
		
	// 4 -----------------------------------------------------

		
		{

			Map<String, String> map = new HashMap<String, String>();

			map.put("tribunal", "Corte Suprema de Justicia");
			map.put("jueces", "J Daniel Aníbal ERBETTA - Roberto Héctor FALISTOCCO - María Angélica GASTALDI - Rafael Francisco GUTIERREZ - Eduardo Guillermo SPULER -");
			map.put("fecha", "05/08/2025");
			map.put("cita", "738/26");
			
			map.put("fuente",
					"Fuente Propia N° de expediente: Año de causa: N° de Tomo / Año: 2026\n" + "	 * N° de página de inicio: 0 N° de página de fin: 0 Resolución N°: 738 Cita:\n" + "	 * 738/26 N° de SAIJ: N° de CUIJ: 21 - 517447 - 0");
			
			
			map.put("N° de Tomo / Año",
					"2025");
			
			RAGSentencia s5 = new RAGSentencia("5", "TASELLI, SERGIO Y OTROS -RECURSO DE INCONSTITUCIONALIDAD EN CARPETA JUDICICAL SERJAL BENINCASA, PATRICIO; LUZZINI, GUSTAVO; TASELLI, MAXIMO; GALLEGOS, MATIAS Y TASELLI, SERGIO s/ APELACION MULTIPROPOSITO-NULIDAD-INVALIDEZ- s/ RECURSO DE INCONSTITUCIONALIDAD (QUEJA ADMITIDA) (RECURSO EXTRAORDINARIO PARA ANTE LA C.S.J.N.)",
					getDateTimeService().parseOffsetDateTime("05/08/2025"), 
					map,
					"En la Provincia de Santa Fe, a los once días del mes de junio del año dos mil veinticinco, los señores Ministros de la Corte Suprema de Justicia de la Provincia, "
					+ "doctores Daniel Aníbal Erbetta, Rafael Francisco Gutiérrez, Eduardo Guillermo Spuler y Margarita Elsa Zabalza, con la presidencia de su titular doctor Roberto Héctor Falistocco, acordaron dictar sentencia en los autos caratulados "
					+ "'SBRISSA, Daniel Antonio contra PROVINCIA DE SANTA FE -RCA- (CUIJ 21-17455462-2) sobre RECURSO DE INCONSTITUCIONALIDAD (PARCIALMENTE CONCEDIDO POR LA CÁMARA)' "
					+ "(Expte. C.S.J. CUIJ N° 21-17455462-2). Se resolvió someter a decisión las siguientes cuestiones: PRIMERA: ¿es admisible el recurso interpuesto? SEGUNDA: en su caso, ¿es procedente? "
					+ "TERCERA: en consecuencia, ¿qué resolución corresponde dictar? Asimismo, se emitieron los votos en el orden que realizaron el estudio de la causa, o sea doctores Gutiérrez, Spuler, Erbetta, Falistocco y Zabalza.");
					
		
			s5.setAnalysis("analisis sentencia 5");
			list.add(s5);
			
		}
		
		
		
		
		
		
	}

	public DateTimeService getDateTimeService() {
		return this.dateService;
	}

	
	/** Performs the actual search (not cached). Every query is logged in the database. */
	protected List<RAGSentencia> executeSearch(String text, OffsetDateTime from, OffsetDateTime to, String subject, User user, String sessionId) {
		return executeSearch(text, from, to, subject, user, sessionId, null, null, null);
	}

	/** Performs the actual search (not cached). Every query is logged in the database with the toolbar options. */
	protected List<RAGSentencia> executeSearch(String text, OffsetDateTime from, OffsetDateTime to, String subject, User user, String sessionId,
			io.demo.results.DateRange dateRange, io.demo.results.SubjectOption subjectOption, io.demo.results.ReasoningEffortOption reasoningEffortOption) {

		long startTime = System.currentTimeMillis();

		// optional filters are passed as RagRequest parameters
		Map<String, String> parameters = new HashMap<>();

		// Solr requires strict ISO-8601 UTC instants (e.g. 2025-09-16T03:00:00Z),
		// so convert the OffsetDateTime to a UTC Instant before sending it
		if (from != null)
			parameters.put("fromDate", from.toInstant().toString());

		if (to != null)
			parameters.put("toDate", to.toInstant().toString());

		if (subject != null && !subject.isBlank())
			parameters.put("subject", subject);

		// request from the RAG server the number of rows determined by the
		// reasoning effort selected in the search form; 0 -> configured default
		int topK = (reasoningEffortOption != null) ? reasoningEffortOption.getTopK() : 0;

		// give the API client the ReasoningEffortOption (both topK and key)172
		RagResponse response = this.kbeeRAGClient.executeQuery(text, parameters.isEmpty() ? null : parameters, topK, reasoningEffortOption);

		long durationMillisecs = System.currentTimeMillis() - startTime;

		// log the query in the database (results = json returned by the server).
		// When there is no user (e.g. regression test run) the query is not
		// logged: the "query" table requires a non-null lastModifiedUser
		if (user != null)
			getQueryLogService().log(text, toJson(response), response.queryId(), durationMillisecs, user, sessionId, dateRange, subjectOption, reasoningEffortOption);
		else
			logger.debug("no user -> query not logged in the database");

		RAGConverter ragConverter = new RAGConverter(response);

		List<RAGSentencia> li = ragConverter.convert();

		return li;
	}

	public String toJson(RagResponse response) {
		try {
			return jsonMapper.writeValueAsString(response);
		} catch (Exception e) {
			logger.error(e, "could not serialize RagResponse");
			return null;
		}
	}

	public QueryLogService getQueryLogService() {
		return this.queryLogService;
	}

	
	
}
