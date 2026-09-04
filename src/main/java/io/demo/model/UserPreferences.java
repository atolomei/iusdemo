package io.demo.model;

import java.util.Map;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "userpreferences")
@Getter
@Setter
@NoArgsConstructor
public class UserPreferences extends DemoDBObject {

	@ManyToOne(fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.DETACH, targetEntity = User.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonManagedReference
	@JsonBackReference
	@JsonProperty("user")
	private User luser;

	@Column(name = "preferences")
	private String preferences;

	/**
	 * @JdbcTypeCode(SqlTypes.JSON) @Column(name = "json_data", columnDefinition =
	 *                              "jsonb") private Map<String, String> settings;
	 **/

}
