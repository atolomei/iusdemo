package io.demo.service.rag;

import java.util.Map;

/**
 * Mirror of {@code kbee.rag.search.RagRequest} from the Kbee RAG Server.
 */
public record RagRequest(
        String question,
        Map<String, String> parameters,
        Integer topK,
        String llm,
        String reasoningEffort) {
}
