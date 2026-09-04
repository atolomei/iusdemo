package io.demo.model.db.service;

import io.demo.model.DemoDBObject;

public class BaseObjectService {

	private final DemoDBObject object;

	public BaseObjectService(DemoDBObject object) {
		this.object = object;
	}

	public DemoDBObject getObject() {
		return object;
	}
}
