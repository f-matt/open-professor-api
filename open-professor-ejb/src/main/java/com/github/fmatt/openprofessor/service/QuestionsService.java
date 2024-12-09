package com.github.fmatt.openprofessor.service;

import com.github.fmatt.openprofessor.model.*;
import com.github.fmatt.openprofessor.utils.CustomRuntimeException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class QuestionsService {

    @Inject
    private Logger logger;

    @PersistenceContext
    private EntityManager entityManager;

    public void saveQuestion(@NotNull Question question) {
        try {
            if (question.getId() == null)
                entityManager.persist(question);
            else    
                entityManager.merge(question);

            // Save related answers
            for (Answer answer : question.getAnswers()) {
                if (answer.getId() != null) {
                    answer.setQuestion(question);
                    entityManager.merge(answer);
                } else {
                    Answer a = new Answer();
                    a.setQuestion(question);
                    a.setCorrect(answer.getCorrect());
                    a.setText(answer.getText());
                    entityManager.persist(a);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error saving question.");
        }
    }

    public List<Question> findQuestions(Integer courseId, Integer section) {
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Question> query = builder.createQuery(Question.class);
            Root<Question> questionRoot = query.from(Question.class);
            questionRoot.fetch(Question_.answers, JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            if (courseId != null) {
                Course course = entityManager.find(Course.class, courseId);
                if (course == null)
                    throw new CustomRuntimeException("Course not found.");

                predicates.add(builder.equal(questionRoot.get(Question_.course), course));
            }

            if (section != null)
                predicates.add(builder.equal(questionRoot.get(Question_.section), section));

            query.where(predicates.toArray(new Predicate[0])).orderBy(builder.asc(questionRoot.get(Question_.id)));

            return entityManager.createQuery(query).getResultList();
        } catch (CustomRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error retrieving questions.");
        }
    }

    public List<Question> findByIdIn(List<Integer> ids) {
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Question> query = builder.createQuery(Question.class);
            Root<Question> questionRoot = query.from(Question.class);
            questionRoot.fetch(Question_.answers, JoinType.LEFT);

            query.where(questionRoot.get(Question_.id).in(ids));

            return entityManager.createQuery(query).getResultList();
        } catch (CustomRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error retrieving questions.");
        }
    }

    public List<Question> findByCourseIdAndSection(Integer courseId, Integer section) {
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Question> query = builder.createQuery(Question.class);
            Root<Question> question = query.from(Question.class);
            Join<Question, Course> course = question.join(Question_.course);
            question.fetch(Question_.answers, JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(course.get(Course_.id), courseId));

            if (section != null)
                predicates.add(builder.equal(question.get(Question_.section), section));

            query.where(predicates.toArray(new Predicate[0]));

            return entityManager.createQuery(query).getResultList();
        } catch (CustomRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error retrieving questions.");
        }
    }

}