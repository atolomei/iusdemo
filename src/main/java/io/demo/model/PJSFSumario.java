package io.demo.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class PJSFSumario extends JsonObject {

	
	@JsonProperty("title")
	private String title;

	
	@JsonProperty("texto")
	private String texto;

	@JsonProperty("fecha")
	private OffsetDateTime fecha;

	@JsonProperty("voces")
	private List<String> voces;



}
