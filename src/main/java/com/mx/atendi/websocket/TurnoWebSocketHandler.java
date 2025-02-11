package com.mx.atendi.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.atendi.service.ITurnoService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class TurnoWebSocketHandler implements WebSocketHandler {

    private final ITurnoService turnoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TurnoWebSocketHandler(ITurnoService turnoService) {
        this.turnoService = turnoService;
    }

    /**
     * Maneja la conexión WebSocket.
     * Se espera que la query string incluya "Tipos-Operacion" (ej.: ?Tipos-Operacion=registro,pago)
     * y que el header "Hospital-Id" esté presente para filtrar los turnos.
     *
     * @param session La sesión WebSocket.
     * @return Mono que indica la finalización del manejo de la sesión.
     */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // Obtener la query string a partir del HandshakeInfo
        final String query = session.getHandshakeInfo().getUri().getQuery();
        final List<String> tiposOperacion ;
        if (query != null && query.contains("Tipos-Operacion=")) {
            String param = query.split("Tipos-Operacion=")[1];
            tiposOperacion = Arrays.asList(param.split(","));
        }else {
        	tiposOperacion = List.of();
        }
        // Obtener el Hospital-Id del header
        String hospitalId = session.getHandshakeInfo().getHeaders().getFirst("Hospital-Id");
        log.info("Nueva conexión WebSocket para hospital {} con operaciones permitidas: {}", hospitalId, tiposOperacion);

        // Filtrar turnos: se envían solo aquellos que estén en estado "pendiente" y
        // que tengan al menos un tipo de operación que coincida con los permitidos.
        Flux<WebSocketMessage> output = turnoService.streamTurnos(hospitalId)
            .filter(turno -> {
                // Asegurarse de que el turno esté pendiente y que la lista de tipos no sea nula.
                if (!"pendiente".equals(turno.getEstado()) || turno.getTipoOperacion() == null) {
                    return false;
                }
                // Retorna true si existe al menos un tipo permitido en la lista del turno.
                return turno.getTipoOperacion().stream()
                        .anyMatch(op -> tiposOperacion.contains(op));
            })
            .map(turno -> {
                try {
                    // Serializa el turno a JSON y crea un mensaje de texto.
                    String json = objectMapper.writeValueAsString(turno);
                    return session.textMessage(json);
                } catch (Exception e) {
                    log.error("Error al serializar turno", e);
                    return session.textMessage("Error al serializar turno");
                }
            });

        // Envía el flujo de mensajes al cliente WebSocket.
        return session.send(output);
    }
}



