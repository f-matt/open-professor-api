package com.github.fmatt.openprofessor.service;

import java.util.logging.Logger;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class PropertiesService {

    @Inject
    private Logger logger;

    private String jwtSecret = null;

    private void loadProperties() {
        // TODO
    }

    public String getJWTSecret() {
        if (jwtSecret == null) 
            loadProperties();

        return jwtSecret;
    }
    
}
