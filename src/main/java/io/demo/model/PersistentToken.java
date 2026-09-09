package io.demo.model;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "token")
@JsonInclude(Include.NON_NULL)
public class PersistentToken extends JsonObject  {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token_id")
	@SequenceGenerator(name = "token_id", sequenceName = "token_id", allocationSize = 1)
	@JsonProperty("id")
	private Long id;
	  
	@Column(name = "entity")
	private String entity;
	
	@Column(name = "object")
	private String object;
	
	@Column(name = "token")
	private String token;
	
	@Column(name = "created")
	@JsonProperty("created")
	private OffsetDateTime created;
	
	@Column(name = "expires")
	@JsonProperty("expires")
	private OffsetDateTime expires;

	public PersistentToken() {
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getEntity() { return entity; }
	public void setEntity(String entity) { this.entity = entity; }

	 

	public String getToken() { return token; }
	public void setToken(String token) { this.token = token; }

	public OffsetDateTime getCreated() { return created; }
	public void setCreated(OffsetDateTime created) { this.created = created; }

	public OffsetDateTime getExpires() { return expires; }
	public void setExpires(OffsetDateTime expires) { this.expires = expires; }

	public String getEntityClass() {
		return object;
	}

	public void setEntityClass(String entityClass) {
		this.object = entityClass;
	}

}

/**
 
  CREATE SEQUENCE token_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
  
  CREATE TABLE token (
    id bigint DEFAULT nextval('token_id') NOT NULL,
    entity character varying(512),
    object character varying(512),
    token character varying(512),
    created timestamp with time zone DEFAULT now() NOT NULL,
    expires timestamp with time zone DEFAULT now() NOT NULL,
    draft text
);

*/


 