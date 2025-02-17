package com.mx.atendi.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
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
	@PostMapping
	@Operation(summary = "Crear un nuevo turno", description = "Solo los administradores y recepcionistas pueden crear turnos")
	public Mono<Turno> crearTurno(@RequestBody Turno turno, Authentication authentication) {
	    String rolUsuario = authentication.getAuthorities().iterator().next().getAuthority();
	    return turnoService.crearTurno(turno, rolUsuario);
	}

	 /**
     * Obtiene los turnos pendientes de un **departamento específico** en tiempo real.
     * Este endpoint está diseñado para los **usuarios de ventanilla**, que solo deben ver los turnos de su departamento.
     *
     * @param hospitalId    ID del hospital donde se encuentran los turnos.
     * @param departamentoId ID del departamento del cual se desean obtener turnos.
     * @return Flux<Turno> con la lista de turnos pendientes en tiempo real para el departamento especificado.
     */
    @GetMapping("/stream/departamento/{hospitalId}/{departamentoId}")
    @Operation(summary = "Ver turnos por departamento", description = "Muestra turnos pendientes solo del departamento asignado")
    public Flux<Turno> streamTurnosPorDepartamento(@PathVariable String hospitalId, @PathVariable String departamentoId) {
        return turnoService.streamTurnos(hospitalId, departamentoId, false);
    }

    /**
     * Proporciona un stream de turnos pendientes filtrados por el tipo de operación (para ventanillas).
     *
     * @param hospitalId    ID del hospital (en header).
     * @param tipoOperacion Tipo de operación para filtrar.
     * @return Flux con los turnos filtrados.
     */
    @Operation(summary = "Stream de turnos por operación", description = "Obtiene un stream de turnos pendientes filtrados por el tipo de operación (para ventanillas)")
 // 🔵 Obtener últimos turnos atendidos para mostrar en la pantalla
    @GetMapping("/ultimos-atendidos/{hospitalId}")
    public Flux<Turno> obtenerTurnosUltimosAtendidos(@PathVariable String hospitalId, @RequestParam(defaultValue = "5") int cantidad) {
        return turnoService.obtenerTurnosUltimosAtendidos(hospitalId, cantidad);
    }
    
    /**
     * Permite que un usuario tome un turno y lo marque como "en proceso".
     * Solo un usuario de ventanilla puede tomar turnos de su propio departamento.
     * Si el turno ya fue tomado por otro usuario, se generará un error.
     *
     * @param turnoId       ID del turno a tomar.
     * @param authentication Información del usuario autenticado que realiza la acción.
     * @return Mono<Turno> con la información del turno actualizado.
     */
    @PutMapping("/{turnoId}/tomar")
    @Operation(summary = "Tomar un turno", description = "Asigna un turno a un usuario dentro de su departamento")
    public Mono<Turno> tomarTurno(@PathVariable String turnoId, Authentication authentication) {
        String usuarioId = authentication.getName();
        String departamentoId = null;
        String hospitalId = null;
        if (authentication.getDetails() instanceof Map) {
        	 Map<String, String> detalles = (Map<String, String>) authentication.getDetails();
             hospitalId = detalles.get("hospitalId");
             departamentoId = detalles.get("departamentoId");
        }
       
        return turnoService.tomarTurno(turnoId, usuarioId, departamentoId);
    }
    
    /**
     * Finaliza un turno, marcándolo como "atendido" o "no atendido".
     * Solo el usuario que tomó el turno puede finalizarlo.
     *
     * @param turnoId       ID del turno a finalizar.
     * @param estadoFinal   Estado final del turno ("atendido" o "no atendido").
     * @param authentication Información del usuario autenticado que realiza la acción.
     * @return Mono<Turno> con la información del turno finalizado.
     */
    @PutMapping("/{turnoId}/finalizar")
    @Operation(summary = "Finalizar un turno", description = "Marca un turno como atendido o no atendido")
    public Mono<Turno> finalizarTurno(@PathVariable String turnoId, @RequestParam String estadoFinal, Authentication authentication) {
        String usuarioId = authentication.getName();
        return turnoService.finalizarTurno(turnoId, usuarioId, estadoFinal);
    }
}
