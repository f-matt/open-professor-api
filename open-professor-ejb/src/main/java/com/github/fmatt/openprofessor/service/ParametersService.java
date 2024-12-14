package com.github.fmatt.openprofessor.service;

import com.github.fmatt.openprofessor.model.Parameter;
import com.github.fmatt.openprofessor.utils.CustomRuntimeException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class ParametersService {

    @Inject
    private Logger logger;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Parameter> findAll () {
        return entityManager.createNamedQuery("Parameter.findAll", Parameter.class).getResultList();
    }

    public void saveParameter(Parameter parameter) {
        if (parameter == null)
            throw new CustomRuntimeException("Parameter is mandatory.");

        if (StringUtils.isBlank(parameter.getName()))
            throw new CustomRuntimeException("Parameter name is mandatory.");

        try {
            Parameter existingParameter = findByName(parameter.getName());
            if (existingParameter != null && !existingParameter.equals(parameter))
                throw new CustomRuntimeException("A parameter with the given name already exists.");

            if (parameter.getId() == null)
                entityManager.persist(parameter);
            else
                entityManager.merge(parameter);
        } catch (CustomRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error saving parameter.");
        }
    }

    public Parameter findByName(String name) {
        try {
            return entityManager.createNamedQuery("Parameter.findByName", Parameter.class)
                    .setParameter("name", name.toUpperCase())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            throw new CustomRuntimeException("More than one parameter found with the given name.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error retrieving parameter.");
        }
    }

    public List<Parameter> findByNameLike(String name) {
        try {
            return entityManager.createNamedQuery("Parameter.findByNameLike", Parameter.class)
                    .setParameter("name", String.format("%%%s%%", name.toUpperCase()))
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error searching parameters.");
        }
    }

}
