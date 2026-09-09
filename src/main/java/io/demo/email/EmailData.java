package io.demo.email;

import io.demo.model.JsonObject;

public class EmailData extends JsonObject {

	public String from;   
	public String to;     
	public String subject; 
	public String msg;
	public String absolutePathAttachments[] = null; // absolute path of files to send

	
	
}
