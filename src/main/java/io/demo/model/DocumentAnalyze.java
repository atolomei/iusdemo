package io.demo.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@JsonInclude(Include.NON_NULL)
@Entity
@Table(name = "documentanalyze")
@Getter
@Setter
@NoArgsConstructor
public class DocumentAnalyze extends DemoDBObject {

	@ManyToOne(fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.DETACH, targetEntity = Query.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "query_id", nullable = false)
	@JsonManagedReference
	@JsonBackReference
	@JsonProperty("query")
	private Query query;

	@Column(name = "results")
	private String results;

	@Column(name = "session_id")
	private String session_id;

	@Column(name = "durationMillisecs")
	long durationMillisecs;

}
