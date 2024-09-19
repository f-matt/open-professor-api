package com.github.fmatt.openprofessor.endpoints;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.fmatt.openprofessor.dto.UsernamePasswordDto;
import com.github.fmatt.openprofessor.service.PropertiesService;
import com.github.fmatt.openprofessor.service.UsersService;
import com.github.fmatt.openprofessor.utils.CustomRuntimeException;
import com.github.fmatt.openprofessor.utils.JwtToken;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthEndpoint {

    @Inject
    private Logger logger;

    @Inject
    private UsersService usersService;

    @Inject
    private PropertiesService propertiesService;

 	@POST
    @Path("/login")
    public Response login(UsernamePasswordDto usernamePasswordDto) {
		if (usernamePasswordDto == null || usernamePasswordDto.getUsername() == null || usernamePasswordDto.getPassword() == null) 
            return Response.status(Response.Status.BAD_REQUEST).entity("Username and password are mandatory.").build();

        try {
			if (usersService.authenticateUser(usernamePasswordDto.getUsername(), usernamePasswordDto.getPassword())) {
                JwtToken jwtToken = createJwtToken(usernamePasswordDto.getUsername());
                return Response.ok(jwtToken).build();
            }

            return Response.status(Response.Status.FORBIDDEN).entity("Invalid credentials.").build();
        } catch (CustomRuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.FORBIDDEN).entity("Error processing authentication.").build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout(@Context HttpServletRequest request) {
        try {
            request.logout();
            return Response.ok().build();
        } catch (ServletException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity("Error performing logout.").build();
        }
    }

    @POST
    @Path("/refresh")
    public Response refresh(@Context HttpServletRequest request) {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorizationHeader == null) 
			return Response.status(Response.Status.NOT_ACCEPTABLE).build();
		
		String token = authorizationHeader.substring("Bearer".length()).trim();
		if (token == null) 
			return Response.status(Response.Status.NOT_ACCEPTABLE).build();
		
		if (!isValid(token)) 
			return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
			String jwtSecret = propertiesService.getJWTSecret();
            if (jwtSecret == null || jwtSecret.isBlank()) 
                throw new CustomRuntimeException("Erro creating access token.");

			Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
			JWTVerifier jwtVerifier = JWT.require(algorithm).build();
			DecodedJWT decoded = jwtVerifier.verify(token);
			
			if (decoded.getSubject() == null || decoded.getSubject().isBlank()) 
				return Response.status(Response.Status.UNAUTHORIZED).build();

            JwtToken jwtToken = createJwtToken(decoded.getSubject());
            return Response.ok(jwtToken).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.FORBIDDEN).entity("Error processing authentication.").build();
        }
    }

    public JwtToken createJwtToken(String username) {
		try {
            String jwtSecret = propertiesService.getJWTSecret();
            if (jwtSecret == null || jwtSecret.isBlank()) 
                throw new CustomRuntimeException("Não foi possível obter a chave JWT.");

			List<String> roles = usersService.findRolesByUsername(username);
			
			Random random = new Random();
			String jwtIdAccess = "";
			for (int i = 0; i < 32; ++i)
				jwtIdAccess += Integer.toHexString(random.nextInt(15));
                
            String jwtIdRefresh = "";
			for (int i = 0; i < 32; ++i)
				jwtIdRefresh += Integer.toHexString(random.nextInt(15));

			Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JwtToken jwtToken = new JwtToken();
			jwtToken.setAccessToken(JWT.create()
				.withExpiresAt(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant())
				.withIssuedAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
				.withNotBefore(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
				.withJWTId(jwtIdAccess)
				.withClaim("sub", username)
				.withClaim("type", "access")
				.withArrayClaim("roles", roles != null ? roles.toArray(new String[0]) : new String[0])
				.sign(algorithm));
            jwtToken.setRefreshToken(JWT.create()
				.withExpiresAt(LocalDateTime.now().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant())
				.withIssuedAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
				.withNotBefore(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
				.withJWTId(jwtIdRefresh)
				.withClaim("sub", username)
				.withClaim("type", "refresh")
				.sign(algorithm));

            return jwtToken;
		} catch (CustomRuntimeException e) {
            throw e;
        } catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CustomRuntimeException("Error creating access token.");
		}
	}

	public boolean isValid(String token) {
		try {
			String jwtSecret = propertiesService.getJWTSecret();
            if (jwtSecret == null || jwtSecret.isBlank()) 
                throw new CustomRuntimeException("Error validating access token.");

			Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

			JWTVerifier jwtVerifier = JWT.require(algorithm).build();
			jwtVerifier.verify(token);

			return true;
		} catch (TokenExpiredException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean checkHasRole(String token, String role) {
		if (token == null || token.isBlank())
			return false;

		if (role == null || role.isBlank())
			return false;

		try {
			String jwtSecret = propertiesService.getJWTSecret();
            if (jwtSecret == null || jwtSecret.isBlank()) 
                throw new CustomRuntimeException("Error verifying access token.");

			Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
			JWTVerifier jwtVerifier = JWT.require(algorithm).build();
			DecodedJWT decoded = jwtVerifier.verify(token);
			
			if (!decoded.getClaim("roles").isMissing() && decoded.getClaim("roles") != null) {
				List<String> roles = Arrays.asList(decoded.getClaim("roles").asArray(String.class));
				return roles.contains(role);
			}

			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
