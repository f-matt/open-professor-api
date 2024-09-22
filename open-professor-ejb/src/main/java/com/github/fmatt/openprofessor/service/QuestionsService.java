package com.github.fmatt.openprofessor.service;

import com.github.fmatt.openprofessor.model.Question;
import com.github.fmatt.openprofessor.model.Question_;
import com.github.fmatt.openprofessor.utils.CustomRuntimeException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;

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
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error saving question.");
        }
    }

    public List<Question> findQuestions() {
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Question> query = builder.createQuery(Question.class);
            Root<Question> questionRoot = query.from(Question.class);

            query.orderBy(builder.asc(questionRoot.get(Question_.id)));

            return entityManager.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error retrieving questions.");
        }
    }
    
}