package io.demo.web.page;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Optional;

import org.apache.wicket.model.IModel;
 
import org.springframework.util.Assert;

import io.demo.model.DemoDBObject;
import io.demo.model.db.service.DBService;
 

public class ObjectModel<T extends DemoDBObject> implements IModel<T> {

	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Class<?> clazza;
	private T object;
	private boolean detached = false;
	
		
	public ObjectModel(T object) {
		this.id=object.getId();
		
		if (clazza==null) {
			String classname = object.getClass().getName();
			int i = classname.indexOf("_");
			if (i>0) classname = classname.substring(0, i);
			i = classname.indexOf("$");
			if (i>0) classname = classname.substring(0, i);
		try {
			clazza = Class.forName(classname);
			} catch (ClassNotFoundException e) {
			 throw new RuntimeException(e);
			}
		}
		setObject(object);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getObject()  {

		if (this.detached) {
			
			if (this.id==null)
				throw new RuntimeException("id is null: " +id +(getObjectClass()!=null ? (" | class:"+getObjectClass().getName()) : " | no class"));
			
			 // DBService<?,Long> service = DBService.getDBService(getObjectClass());
			 // Optional<?> o =	service.findById(getId());
			
			Optional<?> o =	load();
						
			if (o.isPresent()) {
				this.detached = false;
				this.object = (T) o.get();
			}

			if (this.object==null)
				throw new RuntimeException("id: " +id +(getObjectClass()!=null ? (" | class:"+getObjectClass().getName()) : " | no class"));
			
		}	
		return this.object;
	}

	protected Optional<?> load() {
		DBService<?,Long> service = DBService.getDBService(getObjectClass());
		Optional<?> o =	service.findById(getId());
		return o;
	}
	
	protected Class<?> getObjectClass() {
		return this.clazza;
	}
	
	
	public void setObject(T object) {
		this.object = object;
		if (object!=null)
			id = ((DemoDBObject) object).getId();
	}
	
	@Override
	public void detach() {
		try {

			if (detached) 
				return;
			
		 
			if (clazza==null) {
				String classname = object.getClass().getName();
				int i = classname.indexOf("_");
				if (i>0) classname = classname.substring(0, i);
				i = classname.indexOf("$");
				if (i>0) classname = classname.substring(0, i);
				clazza = Class.forName(classname);
			}
			 
			id= ((DemoDBObject)object).getId();
			
			detached = true;
			object = null;
		}
		catch (java.lang.NullPointerException e1 ) {
			detached = true;
		}
		 catch (ClassNotFoundException  e2 ) {
			 throw new RuntimeException(e2);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ObjectModel<?>) {
			return ((ObjectModel<?>)obj).id.equals(id);
		}
		return false;
	}
	
	public Long getId() {
		return id;
	}
	
	@Override
	public int hashCode()    {
		return id.hashCode();
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		if (!detached) {
			detach();
		}	
		if (!detached) {
			Assert.isTrue(detached, "!detached");
		}
		oos.defaultWriteObject();
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
	}
	
}
