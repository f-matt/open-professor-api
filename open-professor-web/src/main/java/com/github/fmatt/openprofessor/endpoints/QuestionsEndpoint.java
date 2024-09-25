package com.github.fmatt.openprofessor.endpoints;

import com.github.fmatt.openprofessor.dto.QuestionIdsDto;
import com.github.fmatt.openprofessor.model.Answer;
import com.github.fmatt.openprofessor.model.Parameter;
import com.github.fmatt.openprofessor.model.Question;
import com.github.fmatt.openprofessor.service.ParametersService;
import com.github.fmatt.openprofessor.service.QuestionsService;
import com.github.fmatt.openprofessor.utils.JaxrsUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

@Path("questions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QuestionsEndpoint {

    @Inject
    private Logger logger;

    @Inject
    private QuestionsService questionsService;

    @Inject
    private ParametersService parametersService;

    @GET
    public Response findQuestions(@QueryParam("course") Integer courseId, @QueryParam("section") Integer section) {
        try {
            List<Question> questions = questionsService.findQuestions(courseId, section);
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

    @POST
    @Path("export-moodle")
    public Response exportMoodle(QuestionIdsDto dto) {
        if (dto == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Selected questions are mandatory.").build();

        if (dto.getQuestionIds() == null || dto.getQuestionIds().isEmpty())
            return Response.status(Response.Status.BAD_REQUEST).entity("At least one question must be selected.").build();

        try {
            Parameter parameter = parametersService.findByName(Parameter.MOODLE_MASK);
            if (parameter == null)
                return Response.status(Response.Status.BAD_REQUEST).entity("Moodle mask parameter not configured.").build();

            List<Question> questions = questionsService.findByIdIn(dto.getQuestionIds());
            StringBuilder content = new StringBuilder();

            for (Question question : questions) {
                String exportedText = parameter.getValue()
                        .replace("{0}", String.valueOf(question.getCourse().getId()))
                        .replace("{1}", String.valueOf(question.getId()))
                        .replace("{2}", question.getText());

                Optional<Answer> correctAnswer = question.getAnswers().stream().filter(Answer::getCorrect).findFirst();
                if (correctAnswer.isEmpty())
                    return Response.status(Response.Status.BAD_REQUEST).entity("Missing correct answer for question " + question.getId() + ".").build();

                exportedText = exportedText.replace("{3}", correctAnswer.get().getText());

                List<Answer> wrongAnswers = question.getAnswers().stream().filter(a -> !a.getCorrect()).toList();
                String[] wrongPlaceholders = {"{4}", "{5}", "{6}"};
                for (int i = 0; i < wrongPlaceholders.length; ++i)
                    exportedText = exportedText.replace(wrongPlaceholders[i], wrongAnswers.get(i).getText());

                content.append(exportedText);
            }

            return Response.ok(content).build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error exporting questions.");
        }
    }

    @POST
    @Path("export-latex")
    public Response exportLatex(QuestionIdsDto dto) {
        if (dto == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Selected questions are mandatory.").build();

        if (dto.getQuestionIds() == null || dto.getQuestionIds().isEmpty())
            return Response.status(Response.Status.BAD_REQUEST).entity("At least one question must be selected.").build();

        try {
            Parameter parameter = parametersService.findByName(Parameter.LATEX_MASK);
            if (parameter == null)
                return Response.status(Response.Status.BAD_REQUEST).entity("Latex mask parameter not configured.").build();

            List<Question> questions = questionsService.findByIdIn(dto.getQuestionIds());
            StringBuilder content = new StringBuilder();
            StringBuilder correctAnswers = new StringBuilder();

            String[] options = { "A", "B", "C", "D" };
            String[] answerPlaceholders = { "{2}", "{3}", "{4}", "{5}" };

            Random random = new Random();

            for (int i = 1; i <= questions.size(); ++i) {
                Question question = questions.get(i - 1);
                String exportedText = parameter.getValue()
                        .replace("{0}", String.valueOf(i))
                        .replace("{1}", question.getText());

                Optional<Answer> correctAnswer = question.getAnswers().stream().filter(Answer::getCorrect).findFirst();
                if (correctAnswer.isEmpty())
                    return Response.status(Response.Status.BAD_REQUEST).entity("Missing correct answer for question " + question.getId() + ".").build();

                List<Answer> wrongAnswers = question.getAnswers().stream().filter(a -> !a.getCorrect()).toList();

                int correctOption = random.nextInt(4);
                correctAnswers.append(String.format("%% %d\t%s\n", i, options[correctOption]));

                int nextWrongAnswer = 0;
                for (int j = 0; j < 4; ++j) {
                    if (j == correctOption)
                        exportedText = exportedText.replace(answerPlaceholders[j], correctAnswer.get().getText());
                    else
                        exportedText = exportedText.replace(answerPlaceholders[j], wrongAnswers.get(nextWrongAnswer++).getText());
                }

                content.append(exportedText);
            }

            content.append(correctAnswers);

            return Response.ok(content).build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error exporting questions.");
        }
    }

}