package com.github.fmatt.openprofessor.service;

import com.github.fmatt.openprofessor.model.Parameter;
import com.github.fmatt.openprofessor.utils.CustomRuntimeException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class PropertiesService {

    @Inject
    private Logger logger;

    @Inject
    private ParametersService parametersService;

    private String jwtSecret = null;

    private void loadProperties() {
        try {
            Parameter parameter = parametersService.findByName(Parameter.PROPERTIES_FILE_PATH);
            Properties properties = getProperties(parameter);

            jwtSecret = properties.getProperty("app.jwtsecret");
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error loading properties file.");
        }
    }

    private static Properties getProperties(Parameter parameter) throws IOException {
        if (parameter == null)
            throw new CustomRuntimeException("Configuration file parameter not set.");

        String propertiesFilePath = parameter.getValue();
        if (propertiesFilePath == null || propertiesFilePath.isBlank())
            throw new CustomRuntimeException("Properties file not found.");

        InputStream inputStream = new FileInputStream(propertiesFilePath);
        Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }

    public String getJWTSecret() {
        if (jwtSecret == null) 
            loadProperties();

        return jwtSecret;
    }
    
}
