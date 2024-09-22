package com.github.fmatt.openprofessor.utils;

import jakarta.ws.rs.core.Response;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JaxrsUtils {

    public static Response processException(Exception e, Logger logger, String defaultMessage) {
        if (e == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Unknown error reached.").build();

        if (e instanceof CustomRuntimeException)
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

        logger.log(Level.SEVERE, e.getMessage(), e);
        return Response.status(Response.Status.BAD_REQUEST).entity(defaultMessage).build();
    }

}
