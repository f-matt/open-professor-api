package com.github.fmatt.openprofessor.service;

import com.github.fmatt.openprofessor.model.Answer;
import com.github.fmatt.openprofessor.model.Course;
import com.github.fmatt.openprofessor.model.Course_;
import com.github.fmatt.openprofessor.utils.CustomRuntimeException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class CoursesService {

    @Inject
    private Logger logger;

    @PersistenceContext
    private EntityManager entityManager;

    public void saveCourse(Course course) {
        try {
            if (course.getId() == null)
                entityManager.persist(course);
            else    
                entityManager.merge(course);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error saving course.");
        }
    }

    public List<Course> findCourses() {
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Course> query = builder.createQuery(Course.class);
            Root<Course> c = query.from(Course.class);

            query.orderBy(builder.asc(c.get(Course_.name)));

            return entityManager.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error retrieving courses.");
        }
    }
    
}