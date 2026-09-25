package io.demo.model;

import java.time.OffsetDateTime;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.demo.results.DateRange;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@JsonInclude(Include.NON_NULL)
@Entity
@Table(name = "query")
@Getter
@Setter
@NoArgsConstructor
public class Query extends DemoDBObject {

	@Column(name = "query")
	String query;

	/**
	 * SHA-256 cache key identifying the query: hash of the normalized query
	 * text + date range + subject + reasoning effort (see QueryLogService).
	 */
	@Column(name = "query_key", length = 64)
	String queryKey;

	@Column(name = "results")
	String results;

	@Column(name = "session_id")
	String session_id;

	@Column(name = "durationMillisecs")
	long durationMillisecs;

	/** max number of results selected in the toolbar (see TotalOption) */
	@Column(name = "totalOption")
	int totalOption = 10;

	/** ordinal of the DateRange selected in the toolbar */
	@Column(name = "dateRangeOption")
	int dateRangeOption = 0;

	/** ordinal of the SubjectOption selected in the toolbar */
	@Column(name = "subjectOption")
	int subjectOption = 0;

	/** ordinal of the DateRange selected in the toolbar */
	@Column(name = "reasoningEffortOption")
	int reasoningEffortOption = 0;

	/** the server's id of the query (see KbeeRAGClient) */
	@Column(name = "serverid", length = 64)
	String serverId;

	/** llm used for the query general analysis */
	@Column(name = "analysisllm", length = 64)
	String analysisLlm;

	/** date the query general analysis was received from the server */
	@Column(name = "analysisdate")
	OffsetDateTime analysisDate;

	/** query general analysis received from the server */
	@Column(name = "analysistext")
	String analysisText;

	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		str.append(" {");
		str.append("\"id\": ").append(getId() != null ? getId().toString() : "null");
		str.append(", \"query\": ").append(query != null ? "\"" + query + "\"" : "null");
		str.append(", \"session_id\": ").append(session_id != null ? "\"" + session_id + "\"" : "null");
		str.append(", \"durationMillisecs\": ").append(durationMillisecs);
		str.append(", \"totalOption\": ").append(totalOption);
		str.append(", \"dateRangeOption\": ").append(dateRangeOption);
		str.append(", \"reasoningEffortOption\": ").append(reasoningEffortOption);
		str.append(", \"subjectOption\": ").append(subjectOption);
		str.append(", \"created\": ").append(getCreated() != null ? "\"" + getCreated().toString() + "\"" : "null");
		str.append(", \"results\": ").append(results != null ? "\"" + (results.length() > 40 ? results.substring(0, 40) + "..." : results) + "\"" : "null");
		str.append("}");
		return str.toString();
	}


	public String getInfo() {

		
		StringBuilder str = new StringBuilder();

		if (getQuery() != null) {
			str.append(getQuery());
		}
 			str.append(" - " + DateRange.fromOrdinal(getDateRangeOption()).getLabel());
		 
		return str.toString();
	}

}
