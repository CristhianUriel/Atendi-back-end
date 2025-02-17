package com.mx.atendi.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

import com.mx.atendi.security.JwtUtil;
import com.mx.atendi.service.ITurnoService;
import com.mx.atendi.websocket.TurnoWebSocketHandler;

@Configuration
public class WebSocketConfig {

	 private final ITurnoService turnoService;
	 private final JwtUtil jwtUtil;

	    public WebSocketConfig(ITurnoService turnoService,JwtUtil jwtUtil) {
	        this.turnoService = turnoService;
	        this.jwtUtil = jwtUtil;
	    }

	    /**
	     * Configura los endpoints de WebSocket y asigna los manejadores correspondientes.
	     *
	     * @return Mapeo de rutas WebSocket con sus manejadores.
	     */
	    @Bean
	    public SimpleUrlHandlerMapping webSocketMapping(ITurnoService turnoService, JwtUtil jwtUtil) {
	        return new SimpleUrlHandlerMapping(Map.of(
	                "/api/turnos/stream/global", new TurnoWebSocketHandler(turnoService, jwtUtil),  // 🔥 Monitor de turnos (ve todo)
	                "/api/turnos/stream/departamento", new TurnoWebSocketHandler(turnoService, jwtUtil) // 🔥 Ventanilla (ve su departamento)
	        ), 1);
	    }

	    /**
	     * Configura el adaptador de WebSocket necesario para manejar las conexiones.
	     *
	     * @return Adaptador de WebSocket.
	     */
	    @Bean
	    public WebSocketHandlerAdapter handlerAdapter() {
	        return new WebSocketHandlerAdapter(webSocketService());
	    }

	    /**
	     * Configura el servicio de WebSocket asegurando que la autenticación de Spring Security se mantenga.
	     *
	     * @return WebSocketService configurado con seguridad.
	     */
	    @Bean
	    public WebSocketService webSocketService() {
	        return new org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService(
	                new ReactorNettyRequestUpgradeStrategy()
	        );
	    }
}

