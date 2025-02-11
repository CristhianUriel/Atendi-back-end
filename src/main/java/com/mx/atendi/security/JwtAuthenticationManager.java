package com.mx.atendi.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

/**
 * Autentica las solicitudes basadas en el token JWT.
 */
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        try {
            Claims claims = jwtUtil.validateToken(authToken);
            String username = claims.getSubject();
            if (username == null) {
                return Mono.empty();
            }
            // Puedes extraer "role" y "hospitalId" de los claims para crear autoridades.
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
            return Mono.just(auth);
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}

