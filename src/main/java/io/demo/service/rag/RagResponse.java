package io.demo.service.rag;

import java.util.List;

/**
 * Mirror of {@code com.kbee.solr.search.RagResponse} from the Kbee RAG Server
 * prototype.
 */
public record RagResponse(
        String question,
        String answer,
        int topK,
        long searchElapsedMilliseconds,
        long elapsedMilliseconds,
        List<VectorSearchResult> sources) {
}
