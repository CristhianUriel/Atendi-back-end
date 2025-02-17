package com.mx.atendi.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Extrae el token JWT del header Authorization y construye el SecurityContext.
 */
@Slf4j
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    private final JwtAuthenticationManager authenticationManager;

    public JwtSecurityContextRepository(JwtAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // No se guarda el contexto ya que es stateless.
        return Mono.error(new UnsupportedOperationException("Not supported"));
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("header : {}",authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String authToken = authHeader.substring(7);
            
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(authToken, null);

            return authenticationManager.authenticate(auth)
                    .map(authenticatedUser -> (SecurityContext) new SecurityContextImpl(authenticatedUser)) // 🔥 Conversión explícita
                    .switchIfEmpty(Mono.empty());
        }
        return Mono.empty();
    }
}

