package com.mx.atendi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.mx.atendi.security.JwtAuthenticationManager;
import com.mx.atendi.security.JwtSecurityContextRepository;
import com.mx.atendi.security.JwtUtil;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, JwtUtil jwtUtil) {
        JwtAuthenticationManager authenticationManager = new JwtAuthenticationManager(jwtUtil);
        JwtSecurityContextRepository securityContextRepository = new JwtSecurityContextRepository(authenticationManager);
        
        return http
                .csrf(csrf -> csrf.disable()
                        .authorizeExchange(exchange -> exchange
                                // Permitir el acceso público a los endpoints de Swagger
                                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .pathMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                                
                                // Permitir endpoints públicos (login, creación de hospitales y usuarios)
                                .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/hospitales/**").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/usuarios/**").permitAll()
                                // Proteger cualquier otro endpoint
                                .anyExchange().authenticated()))
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .build();
    }
}
