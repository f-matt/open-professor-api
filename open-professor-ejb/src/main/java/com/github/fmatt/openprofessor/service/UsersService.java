package com.github.fmatt.openprofessor.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.fmatt.openprofessor.model.*;
import jakarta.persistence.criteria.Join;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.fmatt.openprofessor.utils.CustomRuntimeException;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@Stateless
public class UsersService {

    @Inject
    private Logger logger;

    @PersistenceContext
    private EntityManager entityManager;

    public boolean authenticateUser(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password))
            throw new CustomRuntimeException("Username and password are mandatory.");
        
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> query = builder.createQuery(User.class);
            Root<User> u = query.from(User.class);

            query.where(builder.equal(u.get(User_.username), username));
            User user = entityManager.createQuery(query).getSingleResult();

            if (hashPassword(password).equals(user.getPassword()))
                return true;

            return false;
        } catch (CustomRuntimeException e) {
            throw e;
        } catch (NonUniqueResultException e) {
            logger.severe("More than one user found with login " + username + ".");
            throw new CustomRuntimeException("Error authenticating user.");
        } catch (NoResultException e) {
            return false;
        }
    }

    private String hashPassword(String password) {
        if (StringUtils.isBlank(password))
            return "";

        try {
            byte[] passwordHash = DigestUtils.sha256(password);
			if (passwordHash == null || passwordHash.length < 1)
				throw new CustomRuntimeException("Error generating password hash.");
			
			return Base64.getEncoder().encodeToString(passwordHash);
        } catch (CustomRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error checking credentials.");
        }
    }

    public List<String> findRolesByUsername(String username) {
        if (StringUtils.isBlank(username))
            throw new CustomRuntimeException("Username is mandatory.");

        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Permission> query = builder.createQuery(Permission.class);
            Root<User> u = query.from(User.class);
            Join<User, Role> r = u.join(User_.roles);
            Join<Role, Permission> p = r.join(Role_.permissions);

            query.select(p).distinct(true).where(builder.equal(u.get(User_.username), username));

            List<Permission> permissions = entityManager.createQuery(query).getResultList();
            List<String> permissionNames = new ArrayList<>();

            permissions.forEach(perm -> permissionNames.add(perm.getDescription()));

            return permissionNames;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error retrieving permissions.");
        }
    }
    
}
