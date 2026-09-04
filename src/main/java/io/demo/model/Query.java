package io.demo.model;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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

	@Column(name = "results")
	String results;

	@Column(name = "session_id")
	String session_id;

	@Column(name = "durationMillisecs")
	long durationMillisecs;

}
