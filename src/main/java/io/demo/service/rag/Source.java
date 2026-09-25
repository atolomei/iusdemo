package io.demo.service.rag;

import java.time.OffsetDateTime;
import java.util.List;

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
	        float score
		)
{
}
