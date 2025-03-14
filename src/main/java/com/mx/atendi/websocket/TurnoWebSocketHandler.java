package com.mx.atendi.websocket;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.Authentication;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.atendi.dto.TurnoDTO;
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
        boolean esMonitor = uri.contains("/stream/global");

        // Mantener la recepción de mensajes activa
        Flux<String> incomingMessages = session.receive()
            .map(message -> message.getPayloadAsText())
            .doOnNext(msg -> log.info("📩 Mensaje recibido: {}", msg)) // Log para ver qué recibe el servidor
            .cache(1); // Guarda el primer mensaje en caché para poder reutilizarlo

        return incomingMessages
            .next() // Leer solo el primer mensaje (token)
            .flatMap(token -> {
            	try {
            		ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(token);
                    String newToken = jsonNode.has("token") ? jsonNode.get("token").asText() : null;

                    if (!newToken.startsWith("Bearer ")) {
                        log.warn("❌ Conexión WebSocket rechazada: Token JWT no proporcionado en el mensaje.");
                        return session.close();
                    }

                    newToken = newToken.replace("Bearer ", "");
                    Authentication authentication = jwtUtil.validateToken(newToken);

                    if (authentication == null) {
                        log.warn("❌ Conexión WebSocket rechazada: Token JWT inválido.");
                        return session.close();
                    }

                    String usuarioId = authentication.getName();
                    Map<String, String> detalles = (Map<String, String>) authentication.getDetails();
                    String hospitalId = detalles.get("hospitalId");
                    String departamentoId = detalles.get("departamentoId");

                    log.info("✅ Usuario WebSocket: {} | Hospital: {} | Departamento: {} | Monitor: {}", 
                             usuarioId, hospitalId, departamentoId, esMonitor);

                    Flux<TurnoDTO> turnosStream = turnoService.streamTurnos(hospitalId, esMonitor ? null : departamentoId, esMonitor)
                        .doOnNext(turno -> log.info("📤 Enviando turno: {}", turno))
                        .delayElements(Duration.ofSeconds(1)); // Asegurar que el flujo no se complete de inmediato

                    sessionMap.put(usuarioId, session);

                    return session.send(turnosStream.map(turno -> session.textMessage(turno.toString())))
                                  .then(Mono.never()); // Mantener la conexión abierta
				} catch (Exception e) {
					// TODO: handle exception
					log.error("❌ Error al procesar el JSON: ", e);
			        return session.close();
				}
            })
            .doOnError(error -> log.error("❌ Error en WebSocket: ", error))
            .then();
    }
}
