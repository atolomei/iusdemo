package io.demo.service.rag;

import java.time.OffsetDateTime;

/**
 * Mirror of the segment objects returned by the Kbee RAG Server
 * ({@code /api/rag/answer}), used both as the {@code selected} segment and as
 * the {@code contextSegments} of a {@link Source}.
 * <p>
 * {@code score} is a wrapper type because the server may omit it or send
 * {@code null} (e.g. for context segments without a rerank score).
 * </p>
 */
public record RagSegment(
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
        Float score) {
}
