package io.demo.model;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;

import tools.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.demo.Logger;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 
 * 
 * 
 * CREATE SEQUENCE visit_sequence_id START WITH 100 INCREMENT BY 1 NO MINVALUE
 * NO MAXVALUE CACHE 1;
 * 
 * 
 * CREATE TABLE stat ( id bigint primary key default
 * nextval('visit_sequence_id'), page_id varchar(255) not null, session_id
 * varchar(255) not null, site_id bigint references site(id) on delete cascade,
 * artexhibitionguide_id bigint references artexhibitionguide(id) on delete
 * cascade, guidecontent_id bigint references guidecontent(id) on delete
 * cascade, ts timestamp with time zone DEFAULT now() not null );
 * 
 * 
 * alter table stat add column userAgent character varying(1024);
 * 
 * alter table stat add column artwork_id bigint references artwork(id) on
 * delete cascade,
 * 
 * 
 * 
 * CREATE TABLE pooledString ( id bigint primary key not null, value character
 * varying (2048) );
 * 
 * 
 * alter table stat add column language character varying(24);
 * 
 * alter table stat add column language character varying(24) default 'es';
 * update stat set language='es';
 * 
 * 
 * 
 * 
 */


@Entity
@Table(name = "stat")
@JsonInclude(Include.NON_NULL)
public class Stat extends JsonObject implements Identifiable {

	@JsonIgnore
	static final private ObjectMapper hb6mapper = new DemoObjectMapper();

	@JsonIgnore
	static final private JsonFactory factory = new JsonFactory();

	@JsonIgnore
	static private Logger logger = Logger.getLogger(Stat.class.getName());

	@JsonIgnore
	static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss XXX");

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_visit_id")
	@SequenceGenerator(name = "sequence_visit_id", sequenceName = "sequence_visit_id", allocationSize = 1)
	private Long id;

	@Column(name = "ts")
	private OffsetDateTime timestamp;

	@Column(name = "page_id")
	private String pageId;

	
	@Column(name = "session_id")
	public String sessionId;

	@Column(name = "language")
	private String language;

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Column(name = "userAgent")
	public String userAgent;

	@Transient
	private boolean dependecies = false;

	public Stat() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public String getPageId() {
		return pageId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isDependencies() {
		return dependecies;
	}

	public void setDependencies(boolean b) {
		this.dependecies = b;
	}

	@Override
	@JsonIgnore
	public ObjectMapper getObjectMapper() {
		return hb6mapper;
	}

	protected DateTimeFormatter getDateTimeFormatter() {
		return df;
	}

	protected String baseJSON() {
		StringBuilder str = new StringBuilder();

		str.append(getClass().getSimpleName());

		if (getId() != null)
			str.append("\"id\": " + getId().toString());

		if (this.timestamp != null)
			str.append(", \"timestamp\": \"" + getDateTimeFormatter().format(timestamp) + "\"");

		return str.toString();
	}

	@Override
	public String toString() {

		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		str.append(" { ");
		str.append(baseJSON());
		str.append(" } ");

		return str.toString();
	}

	@Override
	public String toJSON() {
		try {

			return getObjectMapper().writeValueAsString(this);

		} catch (Exception e) {

			logger.error("Serialization does not work if this entity is detached (JPA FetchType.LAZY relationships)");
			return " { \"error\": \"" + e.getClass().getName() + (e.getMessage() != null ? (" | " + e.getMessage().replace("\"", "'" + "\"")) : "") + " }";
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
