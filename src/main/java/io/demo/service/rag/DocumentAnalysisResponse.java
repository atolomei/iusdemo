package io.demo.service.rag;

/**
 * Mirrors the {@code DocumentAnalysisResponse} record returned by the Kbee RAG
 * Server ({@code /api/rag/document-analysis}).
 */
public record DocumentAnalysisResponse(
        String documentId,
        String documentTitle,
        String answer) {
}
