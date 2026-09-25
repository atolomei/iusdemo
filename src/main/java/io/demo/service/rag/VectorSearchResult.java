package io.demo.service.rag;

import java.time.OffsetDateTime;

/**
 * Mirror of {@code com.kbee.solr.search.VectorSearchResult} from the Kbee RAG
 * Server prototype.
 *
 * @deprecated No longer used since the new REST API version; the
 *             {@code /api/rag/answer} endpoint now returns {@link Source}
 *             instances instead. Scheduled for removal.
 */
@Deprecated(forRemoval = true)
public record VectorSearchResult(

		String id,
        String documentId,
        String documentTitle,
        OffsetDateTime documentDate,
        String sectionId,
        String sectionTitle,
        String sectionPath,
        Integer segmentNumber,
        Integer sectionSegmentNumber,
        String text,
        Float score) 

{
	
	
}
