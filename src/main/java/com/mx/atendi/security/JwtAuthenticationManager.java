package com.mx.atendi.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import reactor.core.publisher.Mono;

/**
 * Autentica las solicitudes basadas en el token JWT.
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;


    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getPrincipal().toString();
        log.info("token recibido: {}",authToken);
        try {
            Claims claims = jwtUtil.claimsToken(authToken);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            String hospitalId = claims.get("hospitalId", String.class);
            String departamentoId = claims.get("departamentoId", String.class); // 🔥 Recuperar departamentoId
            log.info("Username: {} role: {} hospitalId: {} departamentoId: {}",username,role,hospitalId,departamentoId);
            if (username == null || role == null || hospitalId == null) {
            	log.info("fallo autenticacion");
                return Mono.empty(); // 🔥 Si falta algún dato, no autenticamos
            }

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

            // 🔥 Guardamos tanto hospitalId como departamentoId en los detalles
            Map<String, String> detalles = Map.of(
                "hospitalId", hospitalId,
                "departamentoId", departamentoId
            );

            auth.setDetails(detalles); // Guardamos ambos valores

            return Mono.just(auth);
        } catch (Exception e) {
            return Mono.empty(); // Token inválido o expirado
        }
    }
}

