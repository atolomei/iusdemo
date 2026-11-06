package io.demo.service.rag;

import java.util.List;

/**
 * Partial mirror of {@code kbee.rag.search.EnhancedQuestion} from the Kbee RAG
 * Server. Concepts and propositions are not needed by the client (unknown JSON
 * properties are ignored).
 */
public record EnhancedQuestion(
		String text,
		String legalText,
		List<String> parties) {
}
