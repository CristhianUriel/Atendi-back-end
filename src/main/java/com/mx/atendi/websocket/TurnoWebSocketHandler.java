package com.mx.atendi.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.mx.atendi.entity.Turno;
import com.mx.atendi.security.JwtUtil;
import com.mx.atendi.service.ITurnoService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class TurnoWebSocketHandler implements WebSocketHandler {
    private final ITurnoService turnoService;
    private final JwtUtil jwtUtil;
    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public TurnoWebSocketHandler(ITurnoService turnoService, JwtUtil jwtUtil) {
        this.turnoService = turnoService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String uri = session.getHandshakeInfo().getUri().toString();
        String authHeader = session.getHandshakeInfo().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("❌ Conexión WebSocket rechazada: Token JWT no proporcionado.");
            return session.close();
        }

        String token = authHeader.replace("Bearer ", "");
        Authentication authentication = jwtUtil.validateToken(token);

        if (authentication == null) {
            log.warn("❌ Conexión WebSocket rechazada: Token JWT inválido.");
            return session.close();
        }

        String usuarioId = authentication.getName();
        Map<String, String> detalles = (Map<String, String>) authentication.getDetails();
        String hospitalId = detalles.get("hospitalId");
        String departamentoId = detalles.get("departamentoId");

        boolean esMonitor = uri.contains("/stream/global");

        log.info("✅ Usuario WebSocket: {} | Hospital: {} | Departamento: {} | Monitor: {}", 
                 usuarioId, hospitalId, departamentoId, esMonitor);

        Flux<Turno> turnosStream = turnoService.streamTurnos(hospitalId, esMonitor ? null : departamentoId, esMonitor);
        
        sessionMap.put(usuarioId, session);

        return session.send(turnosStream.map(turno -> session.textMessage(turno.toString())))
                      .doFinally(signal -> {
                          sessionMap.remove(usuarioId);
                          log.info("🔌 Usuario desconectado: {}", usuarioId);
                      });
    }
}
