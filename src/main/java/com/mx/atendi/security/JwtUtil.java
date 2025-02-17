package com.mx.atendi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public String generateToken(String username, String role, String hospitalId, String departamentoId) {
    	log.info("tiempo {}",new Date(System.currentTimeMillis() + expirationTime).toString());
        return Jwts.builder()
                .claim("role", role)
                .claim("hospitalId", hospitalId)
                .claim("departamentoId", departamentoId) // 🔥 Agregar departamentoId
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
    public Claims claimsToken(String token) {
        try {
        	log.info("Claims");
            return Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.err.println("⚠️ Token expirado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("⚠️ Token mal formado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("⚠️ Token no soportado: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Token vacío o incorrecto: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Valida el token JWT y retorna un objeto Authentication.
     *
     * @param token Token JWT.
     * @return Authentication si el token es válido, null si es inválido.
     */
    public Authentication validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            String hospitalId = claims.get("hospitalId", String.class);
            String departamentoId = claims.get("departamentoId", String.class); // 🔥 Recuperar departamentoId
            log.info("Username: {} role: {} hospitalId: {} departamentoId: {}",username,role,hospitalId,departamentoId);
            if (username == null || role == null || hospitalId == null) {
            	log.info("fallo autenticacion");
                return null; // 🔥 Si falta algún dato, no autenticamos
            }

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

            // 🔥 Guardamos tanto hospitalId como departamentoId en los detalles
            Map<String, String> detalles = Map.of(
                "hospitalId", hospitalId,
                "departamentoId", departamentoId
            );

            auth.setDetails(detalles); // Guardamos ambos valores
            return auth;
        } catch (Exception e) {
            return null; // Token inválido o expirado
        }
    }
}
