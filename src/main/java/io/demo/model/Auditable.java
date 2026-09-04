package io.demo.model;

import java.time.OffsetDateTime;

 

public interface Auditable {

	    public OffsetDateTime getCreated();
	    public OffsetDateTime getLastModified();

	    public User getLastModifiedUser();
	    
	    default public String getObjectClassName() {
			return this.getClass().getSimpleName().toLowerCase();
		}

	    
}
