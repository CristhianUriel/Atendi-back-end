package com.mx.atendi.service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.mx.atendi.entity.Turno;
import com.mx.atendi.repository.TurnoRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
@Slf4j
public class TurnoService implements ITurnoService {

	private final TurnoRepository turnoRepository;
	private final Sinks.Many<Turno> sink;
	// Mapa para contar turnos únicos por (hospital, tipoOperacion)
	private final ConcurrentHashMap<String, AtomicInteger> contadorTurnosPorOperacion;

	public TurnoService(TurnoRepository turnoRepository) {
		this.turnoRepository = turnoRepository;
		// Usamos un Sink que emite y guarda el historial de turnos para nuevos
		// suscriptores.
		this.sink = Sinks.many().replay().all();
		this.contadorTurnosPorOperacion = new ConcurrentHashMap<>();
	}

	/**
     * Crea un turno asignándole un ID basado en la nomenclatura (por ejemplo, "RE-001") y emite el turno creado.
     *
     * @param turno El turno a crear.
     * @return Mono que emite el turno guardado.
     */
    @Override
    public Mono<Turno> crearTurno(Turno turno) {
        turno.setHoraCreacion(LocalDateTime.now());
        turno.setEstado("pendiente");

        // Generamos la clave usando el hospitalId y el primer tipo de operación.
        String op = turno.getTipoOperacion().get(0);
        String key = turno.getHospitalId() + "-" + op;
        // Inicializa el contador si no existe.
        contadorTurnosPorOperacion.putIfAbsent(key, new AtomicInteger(1));
        int numero = contadorTurnosPorOperacion.get(key).getAndIncrement();
        // Genera el código (dos primeras letras en mayúsculas) y forma el ID.
        String opCode = op.substring(0, 2).toUpperCase();
        turno.setId(opCode + "-" + String.format("%03d", numero));

        return turnoRepository.save(turno)
                .doOnNext(savedTurno -> {
                    // Emite el turno creado para que los suscriptores lo reciban.
                    sink.tryEmitNext(savedTurno);
                    log.info("Turno creado y emitido: {}", savedTurno);
                });
    }

    /**
     * Actualiza el estado de un turno y emite la actualización.
     *
     * @param id     ID del turno.
     * @param estado Nuevo estado.
     * @return Mono que emite el turno actualizado.
     */
    @Override
    public Mono<Turno> actualizarEstado(String id, String estado) {
        return turnoRepository.findById(id)
                .flatMap(turno -> {
                    turno.setEstado(estado);
                    turno.setHoraActualizacion(LocalDateTime.now());
                    return turnoRepository.save(turno)
                            .doOnNext(updatedTurno -> {
                                // Emite el turno actualizado.
                                sink.tryEmitNext(updatedTurno);
                                log.info("Turno actualizado y emitido: {}", updatedTurno);
                            });
                });
    }

    /**
     * Retorna un flujo de turnos en tiempo real para un hospital.
     *
     * @param hospitalId ID del hospital.
     * @return Flux con los turnos del hospital.
     */
    @Override
    public Flux<Turno> streamTurnos(String hospitalId) {
        return sink.asFlux()
                .filter(turno -> turno.getHospitalId().equals(hospitalId));
    }

    /**
     * Retorna un flujo de turnos pendientes filtrados por los tipos de operación permitidos para ventanillas.
     *
     * @param hospitalId            ID del hospital.
     * @param tiposOperacionAllowed Iterable con los tipos de operación permitidos.
     * @return Flux con los turnos filtrados.
     */
    @Override
    public Flux<Turno> streamTurnosPorOperacion(String hospitalId, Iterable<String> tiposOperacionAllowed) {
        return sink.asFlux()
                .filter(turno -> turno.getHospitalId().equals(hospitalId)
                        && turno.getEstado().equals("pendiente")
                        && containsAny(turno.getTipoOperacion(), tiposOperacionAllowed));
    }

    /**
     * Verifica si alguno de los tipos de operación del turno coincide con los permitidos.
     *
     * @param turnoOps   Iterable de operaciones del turno.
     * @param allowedOps Iterable de operaciones permitidas.
     * @return true si hay coincidencia; de lo contrario, false.
     */
    private boolean containsAny(Iterable<String> turnoOps, Iterable<String> allowedOps) {
        for (String allowed : allowedOps) {
            for (String op : turnoOps) {
                if (op.equalsIgnoreCase(allowed)) {
                    return true;
                }
            }
        }
        return false;
    }

}
