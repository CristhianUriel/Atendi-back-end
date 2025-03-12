package com.mx.atendi.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.mx.atendi.entity.Turno;
import com.mx.atendi.entity.HistorialTurnos;
import com.mx.atendi.entity.ContadorTurnos;
import com.mx.atendi.repository.TurnoRepository;
import com.mx.atendi.repository.ContadorTurnosRepository;
import com.mx.atendi.repository.HistorialTurnosRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
@Slf4j
public class TurnoService implements ITurnoService {

    private final TurnoRepository turnoRepository;
    private final ContadorTurnosRepository contadorTurnosRepository;
    private final HistorialTurnosRepository historialTurnosRepository;
    private final Sinks.Many<Turno> sink;

    public TurnoService(TurnoRepository turnoRepository, ContadorTurnosRepository contadorTurnosRepository, HistorialTurnosRepository historialTurnosRepository) {
        this.turnoRepository = turnoRepository;
        this.contadorTurnosRepository = contadorTurnosRepository;
        this.historialTurnosRepository = historialTurnosRepository;
        this.sink = Sinks.many().replay().all();
    }

    /**
     * Crea un turno asignándole un identificador alfanumérico único
     * basado en el hospital y departamento, y lo emite en tiempo real.
     *
     * @param turno El turno a crear.
     * @param rolUsuario Rol del usuario que intenta crear el turno.
     * @return Mono<Turno> con el turno guardado.
     */
    @Override
    public Mono<Turno> crearTurno(Turno turno, String rolUsuario) {
        if ("VENTANILLA".equalsIgnoreCase(rolUsuario)) {
            return Mono.error(new RuntimeException("Los usuarios de ventanilla no pueden crear turnos"));
        }

        turno.setHoraCreacion(LocalDateTime.now());
        turno.setEstado("pendiente");

        return generarNumeroTurno(turno.getHospitalId(), turno.getDepartamentoId())
                .flatMap(numeroTurno -> {
                    turno.setNumeroTurno(numeroTurno);
                    return turnoRepository.save(turno)
                            .doOnNext(savedTurno -> {
                                sink.tryEmitNext(savedTurno);
                                log.info("Turno creado y emitido: {}", savedTurno);
                            });
                });
    }

    /**
     * Permite que un usuario tome un turno dentro de su departamento.
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
                    if (!turno.getDepartamentoId().equals(departamentoId)) {
                        return Mono.error(new RuntimeException("No puedes tomar turnos de otro departamento"));
                    }

                    if (turno.getAtendidoPor() != null) {
                        return Mono.error(new RuntimeException("Este turno ya fue tomado por otro usuario"));
                    }

                    turno.setAtendidoPor(usuarioId);
                    turno.setEstado("en proceso");
                    turno.setHoraAtencion(LocalDateTime.now());

                    return turnoRepository.save(turno)
                            .doOnNext(updatedTurno -> {
                                sink.tryEmitNext(updatedTurno);
                                log.info("Turno tomado por usuario {}: {}", usuarioId, updatedTurno);
                            });
                });
    }

    /**
     * Devuelve un flujo de turnos pendientes en tiempo real.
     *
     * @param hospitalId ID del hospital.
     * @param departamentoId ID del departamento (opcional).
     * @param esMonitor Indica si es una vista global o filtrada.
     * @return Flux<Turno> con los turnos en tiempo real.
     */
    @Override
    public Flux<Turno> streamTurnos(String hospitalId, String departamentoId, boolean esMonitor) {
        // 1️ Obtener los turnos pendientes de la base de datos
        Flux<Turno> turnosPendientes = Flux.defer(() -> 
            turnoRepository.findByHospitalIdAndEstado(
                hospitalId,  
                "pendiente"
            )
        );

        // 2️ Emitir turnos nuevos en tiempo real
        Flux<Turno> turnosNuevos = sink.asFlux()
            .filter(turno -> turno.getHospitalId().equals(hospitalId)
                    && turno.getEstado().equals("pendiente")
                    && (esMonitor || turno.getDepartamentoId().equals(departamentoId)));

        // 3️ Combinar ambos flujos: turnos pendientes + turnos nuevos
        return turnosPendientes.concatWith(turnosNuevos);
    }

    /**
     * Obtiene los últimos turnos atendidos para mostrarlos en la pantalla.
     *
     * @param hospitalId ID del hospital.
     * @param cantidad Número de turnos a devolver.
     * @return Flux<Turno> con los últimos turnos atendidos.
     */
    @Override
    public Flux<HistorialTurnos> obtenerTurnosUltimosAtendidos(String hospitalId, int cantidad) {
        return historialTurnosRepository.findAll()
                .filter(turno -> turno.getHospitalId().equals(hospitalId))
                .sort((t1, t2) -> t2.getHoraFinalizacion().compareTo(t1.getHoraFinalizacion()))
                .take(cantidad);
    }

    /**
     * Genera el siguiente número de turno alfanumérico basado en el último número registrado.
     *
     * @param hospitalId ID del hospital.
     * @param departamentoId ID del departamento.
     * @return Mono<String> con el número de turno generado.
     */
    private Mono<String> generarNumeroTurno(String hospitalId, String departamentoId) {
        LocalDate hoy = LocalDate.now();

        return contadorTurnosRepository.findByHospitalIdAndDepartamentoIdAndFecha(hospitalId, departamentoId, hoy)
                .switchIfEmpty(Mono.defer(() -> {
                    ContadorTurnos nuevoContador = new ContadorTurnos();
                    nuevoContador.setHospitalId(hospitalId);
                    nuevoContador.setDepartamentoId(departamentoId);
                    nuevoContador.setFecha(hoy);
                    nuevoContador.setUltimoTurno("A000");
                    return contadorTurnosRepository.save(nuevoContador);
                }))
                .flatMap(contador -> {
                    String nuevoNumero = generarSiguienteNumero(contador.getUltimoTurno());
                    contador.setUltimoTurno(nuevoNumero);
                    return contadorTurnosRepository.save(contador)
                            .map(c -> nuevoNumero);
                });
    }

    private String generarSiguienteNumero(String ultimo) {
        String letras = ultimo.replaceAll("[0-9]", "");
        int numeros = Integer.parseInt(ultimo.replaceAll("[^0-9]", ""));

        if (numeros == 999) {
            letras = siguienteLetra(letras);
            numeros = 1;
        } else {
            numeros++;
        }

        return letras + String.format("%03d", numeros);
    }

    private String siguienteLetra(String letras) {
        if (letras.isEmpty()) return "A";
        char[] chars = letras.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] < 'Z') {
                chars[i]++;
                return new String(chars);
            }
            chars[i] = 'A';
        }
        return "A" + new String(chars);
    }

    /**
     * Reinicia los contadores de turnos al final del día.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetContadoresDiarios() {
        log.info("🔄 Reiniciando contadores de turnos...");
        contadorTurnosRepository.deleteAll().subscribe();
    }

    /**
     * Finaliza un turno, marcándolo como "atendido" o "no atendido".
     * - Mueve el turno a `historial_turnos` para reportes.
     * - Elimina el turno de la colección principal `turnos`.
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
                    if (turno.getAtendidoPor() == null || !turno.getAtendidoPor().equals(usuarioId)) {
                        return Mono.error(new RuntimeException("No puedes finalizar un turno que no tomaste"));
                    }

                    turno.setEstado(estadoFinal);
                    turno.setHoraAtencion(LocalDateTime.now());

                    // 🔥 Guardar el turno en `historial_turnos`
                    HistorialTurnos historial = new HistorialTurnos();
                    historial.setNumeroTurno(turno.getNumeroTurno());
                    historial.setHospitalId(turno.getHospitalId());
                    historial.setDepartamentoId(turno.getDepartamentoId());
                    historial.setTipoOperacion(turno.getTipoOperacion());
                    historial.setEstado(estadoFinal);
                    historial.setAtendidoPor(turno.getAtendidoPor());
                    historial.setHoraCreacion(turno.getHoraCreacion());
                    historial.setHoraAtencion(turno.getHoraAtencion());
                    historial.setHoraFinalizacion(LocalDateTime.now());

                    return historialTurnosRepository.save(historial)
                            .then(turnoRepository.delete(turno)) // 🔥 Elimina el turno activo
                            .thenReturn(turno);
                });
    }
    
    // Método para buscar un turno por su ID
    @Override
    public Mono<Turno> buscarTurnoPorId(String turnoId) {
        return turnoRepository.findById(turnoId)
                .switchIfEmpty(Mono.error(new RuntimeException("Turno no encontrado: " + turnoId)));
    }

    // Método para buscar múltiples turnos por sus IDs
    @Override
    public Flux<Turno> buscarVariosTurnos(List<String> turnosIds) {
        return turnoRepository.findAllById(turnosIds)
                .switchIfEmpty(Mono.error(new RuntimeException("No se encontraron los turnos especificados")));
    }
}
