package io.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.demo.Logger;
import io.demo.service.Jsonable;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * <p>Base class for Classes that can be exported to JSON</p>
 */
public class JsonObject implements Jsonable {

	static private Logger logger =	Logger.getLogger(JsonObject.class.getName());

	@JsonIgnore 
	static final private tools.jackson.databind.ObjectMapper mapper;
	
	//new tools.jackson.databind.ObjectMapper();
	  
	static {
		mapper = JsonMapper.builder()
		        .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
		        .build();
	}
	
	@JsonIgnore 
	public ObjectMapper getObjectMapper() {
		return mapper;
	}

	
	public JsonObject() {
	}
	
	@Override
	public String toString() {
			StringBuilder str = new StringBuilder();
			str.append(this.getClass().getSimpleName());
			str.append(toJSON());
			return str.toString();
	}

	
	
	@Override
	public String toJSON() {
	  try {
			return getObjectMapper().writeValueAsString(this);
		} catch (Exception e) {
				return " { \"error\": \"" + e.getClass().getName() + (e.getMessage()!=null? (" | " + e.getMessage().replace("\"", "'" + "\"")) : "") + " }"; 
		}
	}

}
