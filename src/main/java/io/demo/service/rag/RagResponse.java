package io.demo.service.rag;

import java.util.List;

/**
 * Mirror of {@code kbee.rag.search.RagResponse} from the Kbee RAG Server
 * ({@code /api/rag/answer}).
 */
public record RagResponse(
        String queryId,
        String question,
        String answer,
        List<Source> sources) {
}
