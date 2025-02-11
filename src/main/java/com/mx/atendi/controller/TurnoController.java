package com.mx.atendi.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.atendi.entity.Turno;
import com.mx.atendi.service.ITurnoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Turnos API", description = "Endpoints para la gestión de turnos")
@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*") // Configura CORS según tus necesidades de producción
public class TurnoController {
	private final ITurnoService turnoService;

	public TurnoController(ITurnoService turnoService) {
		this.turnoService = turnoService;
	}
	
	/**
     * Crea un nuevo turno para el hospital.
     *
     * @param turno      Datos del turno a crear.
     * @param hospitalId ID del hospital (se envía en el header).
     * @return Mono con el turno creado.
     */
    @Operation(summary = "Crear turno", description = "Crea un nuevo turno para un hospital")
    @PostMapping
    public Mono<Turno> crearTurno(@RequestBody Turno turno, @RequestHeader("Hospital-Id") String hospitalId) {
        turno.setHospitalId(hospitalId);
        return turnoService.crearTurno(turno);
    }

    /**
     * Actualiza el estado de un turno.
     *
     * @param id     ID del turno.
     * @param estado Nuevo estado del turno.
     * @return Mono con el turno actualizado.
     */
    @Operation(summary = "Actualizar estado", description = "Actualiza el estado de un turno")
    @PutMapping("/{id}/estado")
    public Mono<Turno> actualizarEstado(@PathVariable String id, @RequestParam String estado) {
        return turnoService.actualizarEstado(id, estado);
    }

    /**
     * Proporciona un stream de turnos en tiempo real para un hospital (para monitores).
     *
     * @param hospitalId ID del hospital (en header).
     * @return Flux con los turnos en tiempo real.
     */
    @Operation(summary = "Stream de turnos", description = "Obtiene un stream de turnos en tiempo real para un hospital (para monitores)")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Turno> streamTurnos(@RequestHeader("Hospital-Id") String hospitalId) {
        return turnoService.streamTurnos(hospitalId);
    }

    /**
     * Proporciona un stream de turnos pendientes filtrados por el tipo de operación (para ventanillas).
     *
     * @param hospitalId    ID del hospital (en header).
     * @param tipoOperacion Tipo de operación para filtrar.
     * @return Flux con los turnos filtrados.
     */
    @Operation(summary = "Stream de turnos por operación", description = "Obtiene un stream de turnos pendientes filtrados por el tipo de operación (para ventanillas)")
    @GetMapping(value = "/stream/{tipoOperacion}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Turno> streamTurnosPorOperacion(@RequestHeader("Hospital-Id") String hospitalId,
                                                 @PathVariable String tipoOperacion) {
        return turnoService.streamTurnosPorOperacion(hospitalId, java.util.List.of(tipoOperacion));
    }
}
