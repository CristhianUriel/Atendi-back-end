package com.mx.atendi.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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

		return http.cors(cors -> cors.configurationSource(request -> {
			CorsConfiguration config = new CorsConfiguration();
			config.setAllowedOrigins(List.of("*")); // 🔥 Permite solicitudes desde cualquier origen
			config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
			config.setAllowedHeaders(List.of("Authorization", "Content-Type","Hospital-Id","Accept"));
			//config.setExposedHeaders(List.of("Authorization", "Content-Type","Hospital-Id","Accept"));
			//config.setAllowCredentials(true);
			return config;
		})).csrf(csrf -> csrf.disable()) // ❌ CSRF no es necesario en APIs REST
				.authorizeExchange(exchange -> exchange.pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // ✅
																														// Swagger
																														// público
						.pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll() // ✅ Login público
						.pathMatchers(HttpMethod.GET, "/actuator/**").permitAll() // .hasRole("ADMIN") 🔒 Solo ADMIN
																					// puede ver Actuator
						.pathMatchers(HttpMethod.POST, "/api/hospitales/**").permitAll() // .hasAnyRole("ADMIN",
																							// "SUPERUSER") // 🔒
																							// Restringido
						.pathMatchers(HttpMethod.POST, "/api/usuarios/**").permitAll()// .hasRole("ADMIN") // 🔒 Solo
																						// ADMIN puede crear usuarios
						.pathMatchers(HttpMethod.GET, "/api/usuarios/**").permitAll()// .hasRole("ADMIN") // 🔒 Solo
																						// ADMIN puede crear usuarios
						.pathMatchers("/api/turnos/stream/**").permitAll()
						.pathMatchers(HttpMethod.GET,"/videos/**").permitAll() // Permitir ver videos sin autenticación
	                    .pathMatchers("/videos/upload", "/videos/delete/**").authenticated() // Requiere JWT
						.anyExchange().authenticated() // 🔒 Todo
																												// lo
																												// demás
																												// requiere
																												// autenticación
				).authenticationManager(authenticationManager).securityContextRepository(securityContextRepository)
				.build();
    }
    
	// 🔥 Filtro de CORS global con restricciones más seguras
	//@Bean
//	public CorsWebFilter corsWebFilter() {
//		CorsConfiguration corsConfig = new CorsConfiguration();
//
//		// 🔒 Restringe los dominios permitidos en producción
//		corsConfig.setAllowedOrigins(List.of("http://localhost:4200")); // (List.of("https://miapp.com", "https://admin.miapp.com"));
//		corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//		 corsConfig.setAllowedHeaders(List.of("*"));
//		//corsConfig.setAllowedHeaders(List.of("Authorization", "Content-Type","Hospital-Id"));
//		corsConfig.setExposedHeaders(List.of("Authorization","Content-Type","Hospital-Id"));
//		corsConfig.setAllowCredentials(true);
//		// corsConfig.setExposedHeaders(List.of("Authorization", "Content-Type"));
//
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		source.registerCorsConfiguration("/**", corsConfig);
//		return new CorsWebFilter(source);
//	}
}
