package io.demo.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User feedback of a {@link Query}: a grade ({@link QueryFeedbackGrade}) and
 * an optional free text.
 */
@JsonInclude(Include.NON_NULL)
@Entity
@Table(name = "queryfeedback")
@Getter
@Setter
@NoArgsConstructor
public class QueryFeedback extends DemoDBObject {

	@ManyToOne(fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.DETACH, targetEntity = Query.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "query_id", nullable = false)
	@JsonProperty("query")
	private Query query;

	@Column(name = "info")
	private String info;

	/** int value of the {@link QueryFeedbackGrade}, -1 = no grade */
	@Column(name = "grade")
	private int grade = -1;

	@Transient
	public QueryFeedbackGrade getGradeEnum() {
		return QueryFeedbackGrade.fromValue(grade);
	}

	public void setGradeEnum(QueryFeedbackGrade g) {
		this.grade = (g != null) ? g.getValue() : -1;
	}
}
