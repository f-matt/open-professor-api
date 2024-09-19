package com.github.fmatt.openprofessor.service;

import com.github.fmatt.openprofessor.model.Answer;
import com.github.fmatt.openprofessor.utils.CustomRuntimeException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class AnswersService {

    @Inject
    private Logger logger;

    @PersistenceContext
    private EntityManager entityManager;

    public void saveAnswer(Answer answer) {
        try {
            if (answer.getId() == null)
                entityManager.persist(answer);
            else    
                entityManager.merge(answer);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Erro saving the answer.");
        }
    }
    
}
