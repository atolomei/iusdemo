package io.demo.service.rag;

import java.util.List;

/**
 * Mirrors the {@code RelatedQuestionsResponse} record returned by the Kbee RAG
 * Server ({@code /api/rag/related-questions}).
 */
public record RelatedQuestionsResponse(
        String questionId,
        List<String> questions) {
}
