package io.demo.service.rag;

import java.time.Duration;

/**
 * Mirror of {@code kbee.rag.audit.AuditTimings} from the Kbee RAG Server.
 */
public record AuditTimings(
		Duration total,
		Duration questionEnhancement,
		Duration search,
		Duration sourceExpansion,
		Duration rerank,
		Duration answerGeneration) {
}
