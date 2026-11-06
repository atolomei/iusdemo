package io.demo.service.rag;

import java.time.OffsetDateTime;

/**
 * Mirror of {@code kbee.rag.audit.SearchAuditRequest} from the Kbee RAG Server
 * ({@code POST /api/audit/searches}). All fields are optional.
 */
public record SearchAuditRequest(
		String user,
		OffsetDateTime from,
		OffsetDateTime to,
		Integer limit,
		Integer offset) {
}
