package io.demo.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class ServiceLocator extends BaseService implements ApplicationContextAware {

	static ServiceLocator instance;

	@Autowired
	private ApplicationContext applicationContext;

	static public ServiceLocator getInstance() {
		return instance;
	}

	public ServiceLocator(Settings settings) {
		super(settings);
		instance = this;
	}

	public Object getBean(String name) {
		return getInstance().getApplicationContext().getBean(name);
	}

	public Object getBean(Class<?> clazz) {
		return getInstance().getApplicationContext().getBean(clazz);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

}
