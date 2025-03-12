package com.mx.atendi.controller;

import com.mx.atendi.entity.HistorialTurnos;
import com.mx.atendi.entity.Turno;
import com.mx.atendi.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/turnos")
@Tag(name = "Turnos", description = "Gestión de turnos en tiempo real")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }
    
    /**
     * Crea un nuevo turno.
     * - Solo los usuarios con roles distintos a "VENTANILLA" pueden crear turnos.
     * - Genera un número de turno alfanumérico.
     * - Emite el turno creado en tiempo real vía WebSocket.
     *
     * @param turno Datos del turno a crear.
     * @param authentication Información del usuario autenticado.
     * @return Mono<ResponseEntity<Turno>> con el turno creado.
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo turno", description = "Permite crear un nuevo turno para un hospital y departamento")
    public Mono<Turno> crearTurno(@RequestBody Turno turno, Authentication authentication) {
        String rolUsuario = authentication.getAuthorities().iterator().next().getAuthority();

        return turnoService.crearTurno(turno, rolUsuario)
                .onErrorResume(ex -> {
                    if (ex instanceof RuntimeException && ex.getMessage().contains("no pueden crear turnos")) {
                        return Mono.error(new RuntimeException("FORBIDDEN: Los usuarios de ventanilla no pueden crear turnos"));
                    }
                    return Mono.error(new RuntimeException("INTERNAL_SERVER_ERROR: Error al crear el turno"));
                });
    }

    /**
     * Permite a un usuario tomar un turno de su departamento.
     *
     * @param turnoId ID del turno.
     * @param authentication Información del usuario autenticado.
     * @return Mono<Turno> con el turno actualizado.
     */
    @PutMapping("/{turnoId}/tomar")
    @Operation(summary = "Tomar un turno", description = "Asigna un turno a un usuario dentro de su departamento")
    public Mono<Turno> tomarTurno(@PathVariable String turnoId, Authentication authentication) {
        String usuarioId = authentication.getName();
        String departamentoId = authentication.getAuthorities().iterator().next().getAuthority();
        return turnoService.tomarTurno(turnoId, usuarioId, departamentoId);
    }

    /**
     * Finaliza un turno, marcándolo como atendido o no atendido.
     *
     * @param turnoId ID del turno.
     * @param estadoFinal Estado final del turno ("atendido" o "no atendido").
     * @param authentication Información del usuario autenticado.
     * @return Mono<Turno> con el turno finalizado.
     */
    @PutMapping("/{turnoId}/finalizar")
    @Operation(summary = "Finalizar un turno", description = "Marca un turno como atendido o no atendido")
    public Mono<Turno> finalizarTurno(@PathVariable String turnoId, @RequestParam String estadoFinal, Authentication authentication) {
        String usuarioId = authentication.getName();
        return turnoService.finalizarTurno(turnoId, usuarioId, estadoFinal);
    }

    /**
     * Obtiene los últimos turnos atendidos en el hospital.
     *
     * @param hospitalId ID del hospital.
     * @param cantidad Número de turnos a devolver.
     * @return Flux<Turno> con los últimos turnos atendidos.
     */
    @GetMapping("/ultimos/{hospitalId}/{cantidad}")
    @Operation(summary = "Últimos turnos atendidos", description = "Devuelve una lista de los últimos turnos finalizados en el hospital")
    public Flux<HistorialTurnos> obtenerTurnosUltimosAtendidos(@PathVariable String hospitalId, @PathVariable int cantidad) {
        return turnoService.obtenerTurnosUltimosAtendidos(hospitalId, cantidad);
    }
}
