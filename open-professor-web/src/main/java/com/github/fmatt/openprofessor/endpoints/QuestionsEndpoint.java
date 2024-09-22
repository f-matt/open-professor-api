package com.github.fmatt.openprofessor.endpoints;

import com.github.fmatt.openprofessor.model.Question;
import com.github.fmatt.openprofessor.service.QuestionsService;
import com.github.fmatt.openprofessor.utils.JaxrsUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.logging.Logger;

@Path("questions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QuestionsEndpoint {

    @Inject
    private Logger logger;

    @Inject
    private QuestionsService questionsService;

    @GET
    public Response findQuestions() {
        try {
            List<Question> questions = questionsService.findQuestions();
            return Response.ok(questions).build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error retrieving courses.");
        }
    }

    @POST
    public Response saveQuestion(Question question) {
        if (question == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Question is mandatory.").build();

        try {
            questionsService.saveQuestion(question);
            return Response.ok().build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error saving course.");
        }
    }

}
