package com.github.fmatt.openprofessor.endpoints;

import com.github.fmatt.openprofessor.dto.CourseSectionDto;
import com.github.fmatt.openprofessor.dto.QuestionIdsDto;
import com.github.fmatt.openprofessor.model.Answer;
import com.github.fmatt.openprofessor.model.Parameter;
import com.github.fmatt.openprofessor.model.Question;
import com.github.fmatt.openprofessor.service.ParametersService;
import com.github.fmatt.openprofessor.service.QuestionsService;
import com.github.fmatt.openprofessor.utils.CustomRuntimeException;
import com.github.fmatt.openprofessor.utils.JaxrsUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
                formatMoodle(parameter, question, content);
            }

            return Response.ok(content).build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error exporting questions.");
        }
    }

    private void formatMoodle(Parameter moodleParameter, Question question, StringBuilder content) {
        try {
            String exportedText = moodleParameter.getValue()
                    .replace("{0}", String.valueOf(question.getCourse().getId()))
                    .replace("{1}", String.valueOf(question.getId()))
                    .replace("{2}", question.getText());

            Optional<Answer> correctAnswer = question.getAnswers().stream().filter(Answer::getCorrect).findFirst();
            if (correctAnswer.isEmpty())
                throw new CustomRuntimeException("Missing correct answer for question " + question.getId() + ".");

            exportedText = exportedText.replace("{3}", correctAnswer.get().getText());

            List<Answer> wrongAnswers = question.getAnswers().stream().filter(a -> !a.getCorrect()).toList();
            String[] wrongPlaceholders = {"{4}", "{5}", "{6}"};
            for (int i = 0; i < wrongPlaceholders.length; ++i)
                exportedText = exportedText.replace(wrongPlaceholders[i], wrongAnswers.get(i).getText());

            content.append(exportedText);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error formatting moodle question.");
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

            StringBuilder content = formatLatex(questions);

            return Response.ok(content).build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error exporting questions.");
        }
    }

    @GET
    @Path("export-latex")
    public Response exportLatexByCourseAndSection(@QueryParam("course") Integer courseId,
                                                  @QueryParam("section") Integer section) {
        if (courseId == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Course is mandatory.").build();

        try {
            Parameter parameter = parametersService.findByName(Parameter.LATEX_MASK);
            if (parameter == null)
                return Response.status(Response.Status.BAD_REQUEST).entity("Latex mask parameter not configured.").build();

            List<Question> questions = questionsService.findByCourseIdAndSection(courseId, section);
            if (questions.isEmpty() || questions.size() < 10)
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Must have at least 10 questions in the given course/section.").build();


            Collections.shuffle(questions);
            StringBuilder content = formatLatex(questions.subList(0, 10));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(content.toString().getBytes(StandardCharsets.UTF_8));
            baos.flush();
            baos.close();

            return Response
                    .ok(baos)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"questions.tex\"")
                    .build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error exporting questions.");
        }
    }

    private StringBuilder formatLatex(List<Question> questions) {
        try {
            Parameter parameter = parametersService.findByName(Parameter.LATEX_MASK);
            if (parameter == null)
                throw new CustomRuntimeException("Latex mask parameter not configured.");

            StringBuilder content = new StringBuilder();
            StringBuilder correctAnswers = new StringBuilder();

            String[] options = {"A", "B", "C", "D"};
            String[] answerPlaceholders = {"{2}", "{3}", "{4}", "{5}"};

            Random random = new Random();

            for (int i = 1; i <= questions.size(); ++i) {
                Question question = questions.get(i - 1);
                String exportedText = parameter.getValue()
                        .replace("\\\\", "\\")
                        .replace("{0}", String.valueOf(i))
                        .replace("{1}", question.getText());

                Optional<Answer> correctAnswer = question.getAnswers().stream().filter(Answer::getCorrect).findFirst();
                if (correctAnswer.isEmpty())
                    throw new CustomRuntimeException("Missing correct answer for question " + question.getId() + ".");

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

            return content;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error exporting to latex.");
        }
    }

    @POST
    @Path("export-moodle-and-latex")
    @Produces("application/zip")
    public Response exportMoodleAndLatex(CourseSectionDto courseSectionDto) {
        if (courseSectionDto == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Course and section are mandatory.").build();

        if (courseSectionDto.getCourse() == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Course is mandatory.").build();

        if (courseSectionDto.getSection() == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Section is mandatory.").build();

        try {
            List<Question> questions = questionsService.findQuestions(courseSectionDto.getCourse().getId(),
                    courseSectionDto.getSection());
            if (questions.isEmpty())
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("No question found in the given course/section.")
                        .build();

            Collections.shuffle(questions);

            int pivot = questions.size() / 2;

            Parameter parameterMoodle = parametersService.findByName(Parameter.MOODLE_MASK);
            if (parameterMoodle == null)
                return Response.status(Response.Status.BAD_REQUEST).entity("Moodle mask parameter not configured.").build();

            StringBuilder moodleContent = new StringBuilder();
            for (int i = 0; i < pivot; ++i)
                formatMoodle(parameterMoodle, questions.get(i), moodleContent);

            StringBuilder latexContent = formatLatex(questions.subList(pivot, questions.size()));

            StreamingOutput streamingOutput = outputStream -> {
                ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream));

                ZipEntry zipEntryMoodle = new ZipEntry("moodle.xml");
                zipOutputStream.putNextEntry(zipEntryMoodle);
                zipOutputStream.write(moodleContent.toString().getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();

                ZipEntry zipEntryLatex = new ZipEntry("latex.tex");
                zipOutputStream.putNextEntry(zipEntryLatex);
                zipOutputStream.write(latexContent.toString().getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();

                zipOutputStream.close();
                outputStream.flush();
                outputStream.close();
            };

            return Response
                    .ok(streamingOutput)
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.zip\"")
                    .build();
        } catch (Exception e) {
            return JaxrsUtils.processException(e, logger, "Error exporting questions.");
        }
    }

}