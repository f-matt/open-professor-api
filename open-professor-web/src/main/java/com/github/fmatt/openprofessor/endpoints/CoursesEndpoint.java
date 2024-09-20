package com.github.fmatt.openprofessor.endpoints;

import com.github.fmatt.openprofessor.model.Answer;
import com.github.fmatt.openprofessor.model.Course;
import com.github.fmatt.openprofessor.service.CoursesService;
import com.github.fmatt.openprofessor.utils.CustomRuntimeException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoursesEndpoint {

    @Inject
    private Logger logger;

    @Inject
    private CoursesService coursesService;

    @GET
    public Response findCourses() {
        try {
            List<Course> courses = coursesService.findCourses();
            return Response.ok(courses).build();
        } catch (CustomRuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity("Error retrieving courses.").build();
        }
    }

    @POST
    public Response saveCourse(Course course) {
        if (course == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Course is mandatory.").build();

        try {
            coursesService.saveCourse(course);
            return Response.ok().build();
        } catch (CustomRuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity("Error saving course.").build();
        }
    }

}
