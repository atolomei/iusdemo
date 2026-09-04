package io.demo.service.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.demo.model.Sentencia;

/**
 * Converts a {@link RagResponse} into a {@code List<Sentencia>}.
 *
 * <p>
 * The {@code answer} field of the response is a text containing entries of the
 * form {@code "n. [Fuente x]"} (e.g. {@code "1. [Fuente 3]"}), meaning that
 * element {@code n} of the resulting list is the {@code x}-th document (1-based)
 * of the {@code sources} list. Only sources referenced by a {@code [Fuente x]}
 * entry are included in the result.
 * </p>
 */
public class RAGConverter {

	/** Matches "n. [Fuente x]" (the dot after n is optional). */
	private static final Pattern ENTRY_PATTERN = Pattern.compile("(\\d+)\\.?\\s*\\[Fuente\\s+(\\d+)\\]");

	private final RagResponse response;

	private List<Sentencia> list;

	public RAGConverter(RagResponse response) {
		this.response = response;
	}

	/**
	 * Lazily builds and returns the list of {@link Sentencia} parsed from the
	 * {@link RagResponse}.
	 */
	public List<Sentencia> convert() {
		if (this.list == null)
			this.list = parse();
		return this.list;
	}

	public List<Sentencia> getList() {
		if (this.list == null)
			this.list = parse();
		return this.list;
	}

	private List<Sentencia> parse() {

		List<Sentencia> result = new ArrayList<>();

		if (this.response == null || this.response.answer() == null || this.response.sources() == null)
			return result;

		List<VectorSearchResult> sources = this.response.sources();

		// ordered by the position "n" that appears in the answer
		TreeMap<Integer, VectorSearchResult> ordered = new TreeMap<>();

		Matcher matcher = ENTRY_PATTERN.matcher(this.response.answer());
		while (matcher.find()) {
			int position = Integer.parseInt(matcher.group(1));
			int sourceIndex = Integer.parseInt(matcher.group(2)); // 1-based
			if (sourceIndex >= 1 && sourceIndex <= sources.size())
				ordered.put(position, sources.get(sourceIndex - 1));
		}

		for (VectorSearchResult source : ordered.values()) {
			Sentencia sentencia = new Sentencia();
			
			sentencia.setId(source.documentId());
			sentencia.setRagDocumentId(source.documentId());
			
			
			String s=source.documentId();
			String arr[]=s.split("-");
			
			if (arr[0].equals("fallo") && arr.length>1)
					sentencia.setPjsfDocumentId(arr[1]);
			
			else if (arr[0].equals("sumario") && arr.length>2)
				sentencia.setPjsfDocumentId(arr[2]);
		
			
			sentencia.setRagResponseId(source.id());
			sentencia.setTitle(source.documentTitle());
			sentencia.setFecha(source.documentDate());
			sentencia.setScore(source.score() != null ? source.score() : 0d);
			sentencia.setRelevanceOrder(result.size());
			sentencia.setTribunal("Corte Suprema de Justicia de Santa Fe");
			result.add(sentencia);
			
		}

		return result;
	}
}
