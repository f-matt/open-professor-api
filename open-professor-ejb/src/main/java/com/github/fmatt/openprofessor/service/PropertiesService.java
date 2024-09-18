package com.github.fmatt.openprofessor.service;

import jakarta.ejb.Stateless;

@Stateless
public class PropertiesService {

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
