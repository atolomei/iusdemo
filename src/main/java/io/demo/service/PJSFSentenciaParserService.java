package io.demo.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import io.demo.Logger;
import io.demo.model.PJSFSentencia;
import io.demo.model.PJSFSumario;
import io.demo.util.Check;

/**
 * Parses a sentencia from the Poder Judicial de Santa Fe (PJSF) web portal and
 * generates the {@link PJSFSentencia} (with its {@link PJSFSumario} list) to be
 * saved by the {@link io.demo.model.db.service.PJSFSentenciaDBService}.
 */
@Service
public class PJSFSentenciaParserService extends BaseService {

	static private Logger logger = Logger.getLogger(PJSFSentenciaParserService.class.getName());

	private static final String URL_TEMPLATE = "https://portal.justiciasantafe.gov.ar/bdj/index.php?pg=bus&m=busqueda&c=busqueda&a=get&id=%s";

	private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	/** Argentina offset used for the parsed dates */
	private static final ZoneOffset ZONE_OFFSET = ZoneOffset.ofHours(-3);

	private static final Duration TIMEOUT = Duration.ofSeconds(10);

	private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(TIMEOUT).build();

	public PJSFSentenciaParserService(Settings settings) {
		super(settings);
	}

	/**
	 * Retrieves the document with the given PJSF id (sid) from the web, parses
	 * it and returns the resulting {@link PJSFSentencia} (not saved).
	 */
	public PJSFSentencia fetch(String sid) {

		Check.requireNonNull(sid, "sid is null");

		String text = retrieve(sid);
		PJSFSentencia sentencia = parse(text);
		sentencia.setDocumentoId(sid);
		sentencia.setName("pjsf-sentencia-" + sid);
		return sentencia;
	}

	/** marker of the PJSF error page for a non-existing fallo */
	private static final String NOT_FOUND_MARKER = "El fallo solicitado no existe";

	/**
	 * Parses the (plain) text of a PJSF document previously downloaded to the
	 * file system and returns the resulting {@link PJSFSentencia} (not saved),
	 * or {@code null} if the text is the PJSF "fallo no existe" error page.
	 */
	public PJSFSentencia parseFile(String sid, String text) {

		Check.requireNonNull(sid, "sid is null");
		Check.requireNonNull(text, "text is null");

		if (text.contains(NOT_FOUND_MARKER))
			return null;

		PJSFSentencia sentencia = parse(text);
		sentencia.setDocumentoId(sid);
		sentencia.setName("pjsf-sentencia-" + sid);
		return sentencia;
	}

	/**
	 * Retrieves the raw text of the document from the PJSF portal.
	 */
	protected String retrieve(String sid) {
		String url = String.format(URL_TEMPLATE, sid);
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(TIMEOUT).GET().build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			if (response.statusCode() != 200)
				throw new RuntimeException("Error retrieving PJSF document " + sid + " -> HTTP " + response.statusCode());
			return stripHtml(response.body());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Error retrieving PJSF document " + sid + " from " + url, e);
		}
	}

	/**
	 * Parses the (plain) text of a PJSF document and generates the
	 * {@link PJSFSentencia} with its {@link PJSFSumario} list.
	 */
	public PJSFSentencia parse(String text) {

		Check.requireNonNull(text, "text is null");

		PJSFSentencia s = new PJSFSentencia();

		s.setCaratula(field(text, "Carátula"));
		s.setTribunal(field(text, "Tribunal"));
		s.setJueces(field(text, "Jueces"));
		s.setFuente(field(text, "Fuente"));
		s.setExpediente(field(text, "N° de expediente"));
		s.setAnioCausa(field(text, "Año de causa"));
		s.setTomoAnio(field(text, "N° de Tomo / Año"));
		s.setPaginaInicio(field(text, "N° de página de inicio"));
		s.setPaginaFin(field(text, "N° de página de fin"));
		s.setResolucion(field(text, "Resolución N°"));
		s.setCita(field(text, "Cita"));
		s.setSaij(field(text, "N° de SAIJ"));
		s.setCuij(field(text, "N° de CUIJ"));
		s.setTribunalOrigen(field(text, "Tribunal de origen"));
		s.setTribunalAnterior(field(text, "Tribunal que intervino con anterioridad"));

		String fecha = field(text, "Fecha");
		if (fecha != null && !fecha.isBlank()) {
			try {
				s.setFecha(OffsetDateTime.of(LocalDate.parse(fecha.trim(), FECHA_FORMAT).atStartOfDay(), ZONE_OFFSET));
			} catch (Exception e) {
				logger.warn("Can not parse fecha: " + fecha);
				s.getMetadata().put("fecha", fecha);
			}
		}

		s.setTexto(extractTexto(text));
		s.setSumarios(parseSumarios(text));
		s.getMetadata().put("source", "pjsf");

		return s;
	}

	/**
	 * Extracts the value of a "Label: value" header line.
	 */
	protected String field(String text, String label) {
		Pattern p = Pattern.compile("^\\s*" + Pattern.quote(label) + "\\s*:\\s*(.*?)\\s*$", Pattern.MULTILINE);
		Matcher m = p.matcher(text);
		if (m.find()) {
			String value = m.group(1).trim();
			return value.isEmpty() ? null : value;
		}
		return null;
	}

	/**
	 * The full text of the fallo: between "Texto del fallo:" and either the
	 * "Tribunal de origen"/"Sumarios del fallo" section or the end of the document.
	 */
	protected String extractTexto(String text) {
		Pattern p = Pattern.compile("Texto del fallo\\s*:\\s*(.*?)(?=^\\s*(?:Tribunal de origen\\s*:|Sumarios del fallo)|\\z)", Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = p.matcher(text);
		if (m.find())
			return m.group(1).trim();
		return null;
	}

	/**
	 * Parses the "Sumarios del fallo" section: each sumario is a block of lines
	 * separated by blank lines with the structure:
	 *
	 * <pre>
	 * CONSTITUCIONAL - PENAL                      (materia)
	 * Tesauro &gt; VOZ &gt; SUBVOZ                (voces, one or more lines)
	 * TITLE. IN UPPERCASE. WITH THE VOCES.        (title)
	 * Text of the sumario ...                     (texto)
	 * </pre>
	 */
	protected List<PJSFSumario> parseSumarios(String text) {

		List<PJSFSumario> result = new ArrayList<>();

		Matcher section = Pattern.compile("Sumarios del fallo\\s*\\d*\\s*(.*)\\z", Pattern.DOTALL).matcher(text);
		if (!section.find())
			return result;

		// blocks separated by one or more blank lines
		String[] blocks = section.group(1).split("\\n\\s*\\n");

		PJSFSumario current = null;

		for (String block : blocks) {

			String trimmed = block.strip();
			if (trimmed.isEmpty())
				continue;

			if (trimmed.contains("Tesauro >") || trimmed.contains("Tesauro&gt;")) {

				// a new sumario starts
				if (current != null)
					result.add(current);

				// the fallo files downloaded to the file system have the whole
				// sumario (materia + voces + title + text) on a single line
				if (isSingleLineSumario(trimmed)) {
					current = parseSingleLineSumario(trimmed);
					continue;
				}

				current = new PJSFSumario();
				current.setVoces(new ArrayList<>());

				StringBuilder title = new StringBuilder();
				StringBuilder texto = new StringBuilder();
				boolean vocesSeen = false;

				for (String line : trimmed.split("\\n")) {
					String l = line.strip();
					if (l.isEmpty())
						continue;
					if (l.startsWith("Tesauro")) {
						String voz = l.replaceFirst("^Tesauro\\s*>\\s*", "");
						current.getVoces().add(voz.trim());
						vocesSeen = true;
					} else if (l.equals(l.toUpperCase()) && texto.length() == 0) {
						if (!vocesSeen) {
							// materia line (e.g. "CONSTITUCIONAL - PENAL"): ignored (it is also in the voces)
							continue;
						}
						// title line(s): uppercase lines right after the voces, before the text
						if (title.length() > 0)
							title.append(" ");
						title.append(l);
					} else {
						texto.append(l).append("\n");
					}
				}

				current.setTitle(title.length() > 0 ? title.toString() : null);
				current.setTexto(texto.toString().strip());

			} else if (current != null) {
				// continuation of the text of the current sumario
				String texto = current.getTexto() == null ? "" : current.getTexto() + "\n\n";
				current.setTexto(texto + trimmed);
			}
		}

		if (current != null)
			result.add(current);

		return result;
	}

	/**
	 * The fallo files downloaded to the file system have the whole sumario
	 * (materia + voces + title + text) concatenated on a single line, unlike
	 * the on-demand fetch where {@link #stripHtml(String)} generates one line
	 * per element.
	 */
	protected boolean isSingleLineSumario(String block) {
		String firstLine = block.split("\\n", 2)[0];
		// on the multi-line format the "Tesauro >" voces are on their own lines
		return firstLine.contains("Tesauro >") && !firstLine.strip().startsWith("Tesauro");
	}

	/**
	 * Parses a sumario concatenated on a single line:
	 *
	 * <pre>
	 * MATERIA(S)Tesauro > VOZ &gt; SUBVOZ Tesauro > VOZ ... TITLE. IN UPPERCASE.Text of the sumario ...
	 * </pre>
	 *
	 * The voces are extracted from the "Tesauro >" segments; the body text
	 * starts at the first lowercase word; the uppercase title between the last
	 * voz and the text is separated using the thesaurus terms of the voces.
	 */
	protected PJSFSumario parseSingleLineSumario(String line) {

		PJSFSumario sumario = new PJSFSumario();
		sumario.setVoces(new ArrayList<>());

		String text = line.replace("\n", " ").strip();

		// drop the materia prefix (before the first "Tesauro")
		int first = text.indexOf("Tesauro");
		String rest = text.substring(first);

		// split into "Tesauro > ..." segments
		String[] segments = rest.split("Tesauro\\s*>\\s*");

		// the body text starts at the first word containing a lowercase letter
		String lastSegment = segments[segments.length - 1];
		int textStart = findTextStart(lastSegment);
		String texto = textStart >= 0 ? lastSegment.substring(textStart).strip() : "";
		String upper = (textStart >= 0 ? lastSegment.substring(0, textStart) : lastSegment).strip();

		// voces (all but the tail of the last segment)
		for (int i = 1; i < segments.length - 1; i++)
			sumario.getVoces().add(normalizeVoz(segments[i]));

		// the "upper" zone is: last voz + title sentences separated by ". ".
		// the first '.'-delimited piece mixes the last voz with the first title
		// sentence; the following pieces belong to the title.
		StringBuilder title = new StringBuilder();
		int dot = upper.indexOf('.');
		String mixed = dot >= 0 ? upper.substring(0, dot) : upper;
		String titleTail = dot >= 0 ? upper.substring(dot + 1).strip() : "";

		// separate the last voz from the first title sentence using the
		// thesaurus terms already seen in the voces
		String[] split = splitVozAndTitle(mixed, sumario.getVoces());
		if (!split[0].isBlank())
			sumario.getVoces().add(normalizeVoz(split[0]));
		if (!split[1].isBlank())
			title.append(split[1].strip()).append(".");
		if (!titleTail.isBlank()) {
			if (title.length() > 0)
				title.append(" ");
			title.append(titleTail);
		}

		sumario.setTitle(title.length() > 0 ? title.toString() : null);
		sumario.setTexto(texto);
		return sumario;
	}

	/** index where the body text starts: beginning of the first word containing a lowercase letter, or -1 */
	private int findTextStart(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (Character.isLowerCase(s.charAt(i))) {
				// back up to the start of the word
				int start = i;
				while (start > 0 && Character.isLetter(s.charAt(start - 1)))
					start--;
				return start;
			}
		}
		return -1;
	}

	/**
	 * Splits "LAST VOZ FIRST TITLE SENTENCE" using the thesaurus terms of the
	 * previous voces: the longest suffix equal to a known term is the first
	 * title sentence. Returns {voz, titleSentence}.
	 */
	private String[] splitVozAndTitle(String mixed, List<String> voces) {
		String m = mixed.strip();
		List<String> terms = new ArrayList<>();
		for (String voz : voces)
			for (String t : voz.split(">"))
				terms.add(t.strip());
		String bestVoz = m;
		String bestTitle = "";
		int bestLen = -1;
		for (String term : terms) {
			if (term.isEmpty())
				continue;
			// the term must match a suffix of the mixed piece (word boundary)
			if (m.endsWith(term) && term.length() > bestLen) {
				int cut = m.length() - term.length();
				if (cut == 0 || !Character.isLetter(m.charAt(cut - 1))) {
					bestVoz = m.substring(0, cut).strip();
					bestTitle = term;
					bestLen = term.length();
				}
			}
		}
		return new String[] { bestVoz, bestTitle };
	}

	/** normalizes a voz: collapses whitespace around the '>' separators */
	private String normalizeVoz(String voz) {
		return voz.replaceAll("\\s+", " ").replaceAll("\\s*>\\s*", " > ").strip();
	}

	/**
	 * Removes HTML tags and decodes the most common entities. If the document
	 * is plain text this is a no-op.
	 */
	protected String stripHtml(String html) {
		String text = html;
		text = text.replaceAll("(?i)<br\\s*/?>", "\n");
		text = text.replaceAll("(?i)</(p|div|li|h[1-6]|tr)>", "\n");
		text = text.replaceAll("(?is)<script.*?</script>", "");
		text = text.replaceAll("(?is)<style.*?</style>", "");
		text = text.replaceAll("<[^>]+>", "");
		text = text.replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'").replace("&aacute;", "á").replace("&eacute;", "é").replace("&iacute;", "í").replace("&oacute;", "ó").replace("&uacute;", "ú").replace("&ntilde;", "ñ").replace("&Aacute;", "Á").replace("&Eacute;", "É").replace("&Iacute;", "Í").replace("&Oacute;", "Ó").replace("&Uacute;", "Ú").replace("&Ntilde;", "Ñ");
		return text;
	}

}
