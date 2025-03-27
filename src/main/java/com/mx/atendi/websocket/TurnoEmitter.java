package com.mx.atendi.websocket;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Flux;
import com.mx.atendi.dto.TurnoDTO;

@Component
public class TurnoEmitter {
	private final Sinks.Many<TurnoDTO> sink = Sinks.many().replay().limit(100);

	public void emitirTurno(TurnoDTO turno) {
		sink.tryEmitNext(turno);
	}

	public Flux<TurnoDTO> getFlux() {
		return sink.asFlux();
	}
}
