package com.mx.atendi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Utilidad para generar y validar tokens JWT.
 */
@Component
@Slf4j
public class JwtUtil {

    // En un entorno real, inyecta esta clave desde application.properties o una variable de entorno.
    private final String secret = "bWlDbGF2ZVNlY3JldGFTdXBlclNlZ3VyYUluZ1B1dG8=";
    // Tiempo de expiración del token (ejemplo: 24 horas)
    private final long expirationTime = 86400000L;

    /**
     * Genera un token JWT que incluye el username, rol y hospitalId.
     *
     * @param username   Nombre de usuario.
     * @param role       Rol del usuario.
     * @param hospitalId ID del hospital.
     * @return Token JWT.
     */
    public String generateToken(String username, String role, String hospitalId) {
    	log.info("tiempo {}",new Date(System.currentTimeMillis() + expirationTime).toString());
        return Jwts.builder()
                .claim("role", role)
                .claim("hospitalId", hospitalId)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * Valida el token JWT y retorna los claims.
     *
     * @param token Token JWT.
     * @return Claims extraídos del token.
     */
    public Claims validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
