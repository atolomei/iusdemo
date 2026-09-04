package io.demo.service.rag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the {@code answer} of a {@link DocumentAnalysisResponse}.
 *
 * <p>
 * The answer is a markdown text with the following structure:
 * </p>
 *
 * <pre>
 * ## Por qué es relevante
 * ...analysis text...
 * ## Citas Jurisprudenciales
 * [#1] Corte Suprema de Justicia de Santa Fe. 20/8/1994. Maldonado c/Estado provincial
 * [#2] ...
 * ## Citas Normativas
 * [#1] Ley 24.240. Ley de Defensa del Consumidor. Ley Nacional. 1993. Argentina
 * </pre>
 *
 * <ul>
 * <li>{@code analysis}: text after the title "## Por qué es relevante" and
 * before the title "## Citas Jurisprudenciales"</li>
 * <li>{@code citasJurisprudencia}: each String starts with {@code [#order]}</li>
 * <li>{@code citasNormas}: each String starts with {@code [#order]}</li>
 * </ul>
 */
public class RAGDocumentAnalysisConverter implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Pattern ANALYSIS_PATTERN = Pattern.compile(
			"#{1,6}\\s*Por qué es relevante\\s*(.*?)(?=#{1,6}\\s*Citas Jurisprudenciales|#{1,6}\\s*Citas Normativas|$)",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	private static final Pattern CITAS_JURISPRUDENCIA_PATTERN = Pattern.compile(
			"#{1,6}\\s*Citas Jurisprudenciales\\s*(.*?)(?=#{1,6}\\s*Citas Normativas|$)",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	private static final Pattern CITAS_NORMAS_PATTERN = Pattern.compile(
			"#{1,6}\\s*Citas Normativas\\s*(.*?)(?=#{1,6}\\s|$)",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	/** Matches each citation, i.e. a string starting with "[#n]". */
	private static final Pattern CITA_PATTERN = Pattern.compile(
			"(\\[#\\s*\\d+\\s*\\].*?)(?=\\[#\\s*\\d+\\s*\\]|$)",
			Pattern.DOTALL);

	private String analysis = "";

	private List<String> citasJurisprudencia = new ArrayList<>();

	private List<String> citasNormas = new ArrayList<>();

	public RAGDocumentAnalysisConverter(DocumentAnalysisResponse response) {
		parse(response != null ? response.answer() : null);
	}

	public RAGDocumentAnalysisConverter(String answer) {
		parse(answer);
	}

	public String getAnalysis() {
		return analysis;
	}

	public List<String> getCitasJurisprudencia() {
		return citasJurisprudencia;
	}

	public List<String> getCitasNormas() {
		return citasNormas;
	}

	private void parse(String answer) {

		if (answer == null || answer.isBlank())
			return;

		Matcher analysisMatcher = ANALYSIS_PATTERN.matcher(answer);
		if (analysisMatcher.find()) {
			this.analysis = analysisMatcher.group(1).trim();
		} else {
			// fallback: no "Por qué es relevante" heading -> take everything
			// before the first citas heading
			Matcher firstCitas = Pattern.compile(
					"#{1,6}\\s*Citas (Jurisprudenciales|Normativas)",
					Pattern.CASE_INSENSITIVE).matcher(answer);
			if (firstCitas.find())
				this.analysis = answer.substring(0, firstCitas.start()).trim();
			else
				this.analysis = answer.trim();
		}

		Matcher jurisprudenciaMatcher = CITAS_JURISPRUDENCIA_PATTERN.matcher(answer);
		if (jurisprudenciaMatcher.find())
			this.citasJurisprudencia = parseCitas(jurisprudenciaMatcher.group(1));

		Matcher normasMatcher = CITAS_NORMAS_PATTERN.matcher(answer);
		if (normasMatcher.find())
			this.citasNormas = parseCitas(normasMatcher.group(1));
	}

	/** Splits a section text into citations, each one starting with "[#n]". */
	private List<String> parseCitas(String section) {
		List<String> result = new ArrayList<>();
		if (section == null)
			return result;
		Matcher matcher = CITA_PATTERN.matcher(section);
		while (matcher.find()) {
			String cita = matcher.group(1).trim();
			if (!cita.isBlank())
				result.add(cita);
		}
		return result;
	}
}
