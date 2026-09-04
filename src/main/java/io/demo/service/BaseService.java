package io.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import io.demo.Logger;

 

/**
 * <p>
 * Base class for all Services. Provides access to {@link Settings}.
 * </p>
 *
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
public abstract class BaseService {

    @SuppressWarnings("unused")
    static private Logger logger = Logger.getLogger(BaseService.class.getName());

    @Autowired
    private Settings settings;

    public BaseService(Settings settings) {
    
    this.settings = settings;
    }
    

    public Settings getSettings() {
        return settings;
    }
}
