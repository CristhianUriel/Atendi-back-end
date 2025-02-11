package com.mx.atendi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.mx.atendi.service.ITurnoService;
import com.mx.atendi.websocket.TurnoWebSocketHandler;

import java.util.Map;

@Configuration
public class WebSocketConfig {

    /**
     * Crea el handler para gestionar las conexiones WebSocket de turnos.
     *
     * @param turnoService Servicio de turnos.
     * @return Instancia de TurnoWebSocketHandler.
     */
    @Bean
    public TurnoWebSocketHandler turnoWebSocketHandler(ITurnoService turnoService) {
        return new TurnoWebSocketHandler(turnoService);
    }

    /**
     * Mapea la URL "/ws/turnos" al handler de WebSocket.
     *
     * @param turnoWebSocketHandler Handler para WebSocket.
     * @return HandlerMapping con la asignación.
     */
    @Bean
    public HandlerMapping handlerMapping(TurnoWebSocketHandler turnoWebSocketHandler) {
        return new SimpleUrlHandlerMapping(Map.of("/ws/turnos", turnoWebSocketHandler), 10);
    }

    /**
     * Crea un adaptador para el manejo de WebSocket.
     *
     * @return WebSocketHandlerAdapter.
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}

