package io.demo.model;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;

import io.demo.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@JsonInclude(Include.NON_NULL)
public class DemoDBObject extends DemoObject implements Identifiable, Auditable {

	@JsonIgnore
	static final private DemoObjectMapper hb6mapper = new DemoObjectMapper();

	@JsonIgnore
	static final private JsonFactory factory = new JsonFactory();

	@JsonIgnore
	static private Logger logger = Logger.getLogger(DemoDBObject.class.getName());

	@JsonIgnore
	static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss XXX");

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_gen")
	@SequenceGenerator(name = "sequence_gen", sequenceName = "sequence_id", allocationSize = 1)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "created")
	private OffsetDateTime created;

	@Column(name = "lastModified")
	private OffsetDateTime lastModified;

	@ManyToOne(fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.DETACH, targetEntity = User.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastModifiedUser", nullable = false)
	@JsonManagedReference
	@JsonBackReference
	@JsonProperty("lastModifiedUser")
	private User lastModifiedUser;

	@Column(name = "state")
	@Enumerated(EnumType.ORDINAL)
	private ObjectState state;

	
	@Transient
	private boolean dependecies = false;

	
	public boolean isDependencies() {
		return dependecies;
	}

	public void setDependencies(boolean b) {
		this.dependecies = b;
	}

	
	
	@Override
	public String getTitle() {
		return name;
	}

	protected DateTimeFormatter getDateTimeFormatter() {
		return df;
	}

	
}
