package io.demo.service.rag;

import java.util.ArrayList;
import java.util.List;

import io.demo.model.RAGSentencia;

/**
 * Converts a {@link RagResponse} into a {@code List<RAGSentencia>}.
 *
 * <p>
 * The new REST API returns the {@code sources} already ranked by relevance
 * (descending {@code score}), so the conversion is a straightforward mapping
 * of each {@link Source} to a {@link RAGSentencia}.
 * </p>
 */
public class RAGConverter {

	private final RagResponse response;

	private List<RAGSentencia> list;

	public RAGConverter(RagResponse response) {
		this.response = response;
	}

	/**
	 * Lazily builds and returns the list of {@link RAGSentencia} parsed from the
	 * {@link RagResponse}.
	 */
	public List<RAGSentencia> convert() {
		if (this.list == null)
			this.list = parse();
		return this.list;
	}

	public List<RAGSentencia> getList() {
		return convert();
	}

	private List<RAGSentencia> parse() {

		List<RAGSentencia> result = new ArrayList<>();

		if (this.response == null || this.response.sources() == null)
			return result;

		for (Source source : this.response.sources()) {

			// skip malformed sources (no selected segment or document id)
			if (source == null || source.documentId() == null)
				continue;

			RAGSentencia sentencia = new RAGSentencia();

			sentencia.setId(source.documentId());
			sentencia.setRagDocumentId(source.documentId());

			String s = source.documentId();
			String arr[] = s.split("-");

			if (arr[0].equals("fallo") && arr.length > 1)
				sentencia.setPjsfDocumentId(arr[1]);

			else if (arr[0].equals("sumario") && arr.length > 2)
				sentencia.setPjsfDocumentId(arr[2]);

			sentencia.setTitle(source.documentTitle());
			sentencia.setFecha(source.documentDate());
			sentencia.setScore(source.score());
			sentencia.setRelevanceOrder(result.size());
			sentencia.setTribunal("Corte Suprema de Justicia de Santa Fe");
			result.add(sentencia);
		}

		return result;
	}
}
