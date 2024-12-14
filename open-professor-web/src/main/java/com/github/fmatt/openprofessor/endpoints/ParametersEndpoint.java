package com.github.fmatt.openprofessor.endpoints;

import com.github.fmatt.openprofessor.model.Parameter;
import com.github.fmatt.openprofessor.service.ParametersService;
import com.github.fmatt.openprofessor.utils.JaxrsUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.logging.Logger;

@Path("parameters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ParametersEndpoint {

    @Inject
    private Logger logger;

    @Inject
    private ParametersService parametersService;

    @GET
    public Response findParameters(@QueryParam("name") String name) {
        try {
            List<Parameter> parameters = parametersService.findByNameLike(name);
            return Response.ok(parameters).build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error retrieving courses.");
        }
    }

    @POST
    public Response saveParameter(Parameter parameter) {
        if (parameter == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Parameter is mandatory.").build();

        try {
            parametersService.saveParameter(parameter);
            return Response.ok().build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error saving parameter.");
        }
    }

    @PATCH
    public Response updateParameter(Parameter parameter) {
        if (parameter == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Parameter is mandatory.").build();

        if (parameter.getId() == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Parameter key is mandatory.").build();

        try {
            parametersService.saveParameter(parameter);
            return Response.ok().build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error saving parameter.");
        }
    }

}