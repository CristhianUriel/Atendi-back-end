package com.mx.atendi.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.scheduling.annotation.Scheduled;
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
	private final ConcurrentHashMap<String, AtomicInteger> contadorTurnosPorDepartamento;
	private LocalDate fechaUltimoReset;
	
	public TurnoService(TurnoRepository turnoRepository) {
		this.turnoRepository = turnoRepository;
		// Usamos un Sink que emite y guarda el historial de turnos para nuevos
		// suscriptores.
		this.sink = Sinks.many().replay().all();
		this.contadorTurnosPorDepartamento = new ConcurrentHashMap<>();
		this.fechaUltimoReset = LocalDate.now();
	}

	/**
     * Crea un turno asignándole un ID basado en la nomenclatura (por ejemplo, "RE-001") y emite el turno creado.
     *
     * @param turno El turno a crear.
     * @return Mono que emite el turno guardado.
     */
    @Override
    public Mono<Turno> crearTurno(Turno turno, String rolUsuario) {
        // Solo los administradores y recepcionistas pueden crear turnos
        if ("VENTANILLA".equals(rolUsuario)) {
            return Mono.error(new RuntimeException("Los usuarios de ventanilla no pueden crear turnos"));
        }

        turno.setHoraCreacion(LocalDateTime.now());
        turno.setEstado("pendiente");

        // Reiniciar el contador de turnos si es un nuevo día
        verificarYReiniciarContador(turno.getHospitalId(), turno.getDepartamentoId());

        // Generar un identificador alfanumérico para el turno
        String key = turno.getHospitalId() + "-" + turno.getDepartamentoId();
        int numero = contadorTurnosPorDepartamento.get(key).getAndIncrement();
        String letraTurno = generarCodigoAlfanumerico(numero);
        
        turno.setId(letraTurno + "-" + String.format("%03d", numero));

        return turnoRepository.save(turno)
                .doOnNext(savedTurno -> {
                    sink.tryEmitNext(savedTurno);
                    log.info("Turno creado y emitido: {}", savedTurno);
                });
    }

    /**
     * Permite que un usuario tome un turno dentro de su departamento.
     * - Solo los usuarios del mismo departamento pueden tomar el turno.
     * - Un turno solo puede ser tomado si está en estado "pendiente".
     * - Si otro usuario ya tomó el turno, se devuelve un error.
     *
     * @param turnoId ID del turno a tomar.
     * @param usuarioId ID del usuario que desea tomar el turno.
     * @param departamentoId ID del departamento del usuario.
     * @return Mono<Turno> con el turno actualizado si la operación fue exitosa.
     */
    @Override
    public Mono<Turno> tomarTurno(String turnoId, String usuarioId, String departamentoId) {
        return turnoRepository.findById(turnoId)
                .flatMap(turno -> {
                    // Verificar si el turno pertenece al mismo departamento del usuario
                    if (!turno.getDepartamentoId().equals(departamentoId)) {
                        return Mono.error(new RuntimeException("No puedes tomar turnos de otro departamento"));
                    }

                    // Verificar si el turno ya fue tomado por otro usuario
                    if (turno.getUsuarioAtendidoId() != null) {
                        return Mono.error(new RuntimeException("Este turno ya fue tomado por otro usuario"));
                    }

                    // Asignar el turno al usuario y actualizar su estado
                    turno.setUsuarioAtendidoId(usuarioId);
                    turno.setEstado("en proceso");
                    turno.setHoraActualizacion(LocalDateTime.now());

                    return turnoRepository.save(turno)
                            .doOnNext(updatedTurno -> {
                                // Emitir el turno actualizado para que se refleje en tiempo real
                                sink.tryEmitNext(updatedTurno);
                                log.info("Turno tomado por usuario {}: {}", usuarioId, updatedTurno);
                            });
                });
    }

    /**
     * Finaliza un turno, marcándolo como atendido o no atendido.
     *
     * @param turnoId ID del turno.
     * @param usuarioId ID del usuario que atendió el turno.
     * @param estadoFinal Estado final del turno ("atendido" o "no atendido").
     * @return Mono<Turno> con el turno finalizado.
     */
    @Override
    public Mono<Turno> finalizarTurno(String turnoId, String usuarioId, String estadoFinal) {
        return turnoRepository.findById(turnoId)
                .flatMap(turno -> {
                    if (!turno.getUsuarioAtendidoId().equals(usuarioId)) {
                        return Mono.error(new RuntimeException("No puedes finalizar un turno que no tomaste"));
                    }
                    turno.setEstado(estadoFinal);
                    turno.setHoraActualizacion(LocalDateTime.now());
                    return turnoRepository.save(turno)
                            .doOnNext(updatedTurno -> {
                                sink.tryEmitNext(updatedTurno);
                                log.info("Turno finalizado por usuario {}: {}", usuarioId, updatedTurno);
                            });
                });
    }


    /**
     * Devuelve un flujo de turnos pendientes en tiempo real.
     *
     * @param hospitalId ID del hospital
     * @return Flux con los turnos pendientes
     */
    @Override
    public Flux<Turno> streamTurnos(String hospitalId, String departamentoId, boolean esMonitor) {
        return sink.asFlux()
                .filter(turno -> turno.getHospitalId().equals(hospitalId)
                        && turno.getEstado().equals("pendiente")
                        && (esMonitor || turno.getDepartamentoId().equals(departamentoId))); 
    }

    /**
     * Devuelve los últimos turnos atendidos para mostrarlos en la pantalla.
     *
     * @param hospitalId ID del hospital
     * @param cantidad Número de turnos a devolver
     * @return Flux con los últimos turnos atendidos
     */
    @Override
    public Flux<Turno> obtenerTurnosUltimosAtendidos(String hospitalId, int cantidad) {
        return turnoRepository.findAll()
                .filter(turno -> turno.getHospitalId().equals(hospitalId) && !turno.getEstado().equals("pendiente"))
                .sort((t1, t2) -> t2.getHoraActualizacion().compareTo(t1.getHoraActualizacion()))
                .take(cantidad);
    }
    
    
    
    
    
    private void verificarYReiniciarContador(String hospitalId, String departamentoId) {
        String key = hospitalId + "-" + departamentoId;
        LocalDateTime now = LocalDateTime.now();

        // Si es un nuevo día, reiniciar el contador
        if (!contadorTurnosPorDepartamento.containsKey(key) || now.getHour() == 0) {
            contadorTurnosPorDepartamento.put(key, new AtomicInteger(0)); // Reinicia en 0
            log.info("Se reinició la numeración de turnos para {} en el hospital {}", departamentoId, hospitalId);
        }
    }

    /**
     * Reinicia automáticamente los contadores de turnos a medianoche.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetContadoresDiarios() {
        log.info("🔄 Reiniciando contadores de turnos (Reset diario automático)...");
        contadorTurnosPorDepartamento.clear();
        fechaUltimoReset = LocalDate.now();
    }

    /**
     * Permite resetear manualmente los contadores de turnos.
     *
     * @return Mono vacío cuando el reset se completa
     */
    public Mono<Void> resetContadoresManualmente() {
        log.info("🔄 Reiniciando contadores de turnos (Reset manual)...");
        contadorTurnosPorDepartamento.clear();
        fechaUltimoReset = LocalDate.now();
        return Mono.empty();
    }
    
    private String generarCodigoAlfanumerico(int numero) {
        StringBuilder codigo = new StringBuilder();

        // Convertir número a secuencia alfabética
        int base = 26; // Letras del abecedario
        while (numero >= 0) {
            codigo.insert(0, (char) ('A' + (numero % base)));
            numero = (numero / base) - 1;
        }

        return codigo.toString();
    }	

}
