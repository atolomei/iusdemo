package io.demo.service.rag;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Mirror of {@code kbee.rag.audit.SearchAuditResponse} from the Kbee RAG Server
 * ({@code POST /api/audit/searches}).
 */
public record SearchAuditResponse(
		UUID id,
		String userId,
		String llm,
		OffsetDateTime startedAt,
		OffsetDateTime finishedAt,
		AuditStatus result,
		String error,
		String question,
		Integer requestedTopK,
		EnhancedQuestion enhancedQuestion,
		RagResponse response,
		AuditTimings timings) {
}
