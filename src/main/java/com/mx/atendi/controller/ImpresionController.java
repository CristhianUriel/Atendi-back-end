package com.mx.atendi.controller;

import com.mx.atendi.entity.Turno;
import com.mx.atendi.service.IImpresionService;
import com.mx.atendi.service.ITurnoService;
import com.mx.atendi.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/impresion")
@Tag(name = "Impresión", description = "Servicio para imprimir tickets de turnos")
public class ImpresionController {

	private final IImpresionService impresionService;
	private final ITurnoService turnoService;

	public ImpresionController(IImpresionService impresionService, TurnoService turnoService) {
		this.impresionService = impresionService;
		this.turnoService = turnoService;
	}

	// 🔹 POST con RequestBody en lugar de PathVariable y RequestParam
	@PostMapping("/tickets")
    @Operation(summary = "Imprimir uno o varios tickets de turno")
    public Mono<ResponseEntity<String>> imprimirTicket(@RequestBody TicketRequest request) {
        // 🔹 Si se envió un solo turno
        if (request.getTurnoId() != null) {
            return turnoService.buscarTurnoPorId(request.getTurnoId())
                    .flatMap(turno -> impresionService.imprimirTicket(turno, request.getImpresora()))
                    .map(resultado -> ResponseEntity.ok().body(resultado))
                    .defaultIfEmpty(ResponseEntity.badRequest().body("❌ No se encontró el turno."));
        }

        // 🔹 Si se envió una lista de turnos
        if (request.getTurnosIds() != null && !request.getTurnosIds().isEmpty()) {
            return turnoService.buscarVariosTurnos(request.getTurnosIds()).collectList()
                    .flatMap(turnos -> impresionService.imprimirVariosTickets(turnos, request.getImpresora()))
                    .map(resultado -> ResponseEntity.ok().body(resultado))
                    .defaultIfEmpty(ResponseEntity.badRequest().body("❌ No se encontraron turnos válidos."));
        }

        // 🔹 Si no se envió nada válido
        return Mono.just(ResponseEntity.badRequest().body("❌ Debes proporcionar un turnoId o una lista de turnosIds."));
    }

	// 🔹 Clase interna para manejar la estructura del RequestBody
	@Data
	public static class TicketRequest {
		 private String turnoId;      // Para un solo ticket
	        private List<String> turnosIds; // Para múltiples tickets
	        private String impresora;
	}

	@GetMapping("/impresoras")
	@Operation(summary = "Listar impresoras disponibles")
	public Mono<List<String>> listarImpresorasDisponibles() {
		return impresionService.listarImpresorasDisponibles();
	}
}
