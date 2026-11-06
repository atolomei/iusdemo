package io.demo.service.rag;

import java.time.OffsetDateTime;
import java.util.List;

import io.demo.model.LegalStance;

/**
 * Mirror of {@code kbee.rag.search.Source} from the Kbee RAG Server.
 * <p>
 * Since the server response format changed, a source is a wrapper around the
 * {@code selected} segment plus its surrounding {@code contextSegments}.
 * Convenience accessors delegate to the selected segment (null-safe) so
 * existing callers keep working.
 * </p>
 */
public record Source(
		  String documentId,
	        String documentTitle,
	        OffsetDateTime documentDate,
	        /** query level: 0, 1 or 2 (see io.demo.results.QueryLevelOption) */
	        Integer level,
	        float score,
	        LegalStance stance

		
		
		)
{
	/** a missing / null level (e.g. old cached responses) defaults to 0 (RELEVANCIA DIRECTA) */
	public Source {
		if (level == null)
			level = 0;
		if (stance == null)
			stance = LegalStance.NO_DEFINIDO;
	}
}
