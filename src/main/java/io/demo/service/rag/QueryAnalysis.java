package io.demo.service.rag;

import java.time.OffsetDateTime;

/**
 * Mirror of {@code kbee.rag.search.QueryAnalysis} from the Kbee RAG Server
 * ({@code /api/rag/queryanalysis}).
 */
public record QueryAnalysis(
        String queryId,
        String llm,
        OffsetDateTime dateCreated,
        long totalTime,
        String analysis) {
}
