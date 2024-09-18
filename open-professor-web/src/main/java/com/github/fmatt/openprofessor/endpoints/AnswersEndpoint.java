package com.github.fmatt.openprofessor.endpoints;

import com.github.fmatt.openprofessor.model.Answer;
import com.github.fmatt.openprofessor.service.AnswersService;
import com.github.fmatt.openprofessor.utils.CustomRuntimeException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/answers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnswersEndpoint {

    @Inject
    private Logger logger;

    @Inject
    private AnswersService answersService;

    @POST
    public Response saveAnswer(Answer answer) {
        if (answer == null)
            return Response.status(Status.BAD_REQUEST).entity("Answer is mandatory.").build();

        try {
            answersService.saveAnswer(answer);
            return Response.ok().build();
        } catch (CustomRuntimeException e) {
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Status.BAD_REQUEST).entity("Error saving answer.").build();
        }
    }
    
}
