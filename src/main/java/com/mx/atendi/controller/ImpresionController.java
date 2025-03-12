package com.mx.atendi.controller;

import com.mx.atendi.entity.Turno;
import com.mx.atendi.service.IImpresionService;
import com.mx.atendi.service.ITurnoService;
import com.mx.atendi.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @GetMapping("/ticket/{turnoId}")
    @Operation(summary = "Imprimir ticket de turno")
    public Mono<String> imprimirTicket(@PathVariable String turnoId, @RequestParam(required = false) String impresora) {
        return turnoService.buscarTurnoPorId(turnoId)
                .flatMap(turno -> impresionService.imprimirTicket(turno, impresora));
    }

    @PostMapping("/tickets")
    @Operation(summary = "Imprimir múltiples tickets")
    public Mono<String> imprimirVariosTickets(@RequestBody List<String> turnosIds, @RequestParam(required = false) String impresora) {
        return turnoService.buscarVariosTurnos(turnosIds)
                .collectList()
                .flatMap(turnos -> impresionService.imprimirVariosTickets(turnos, impresora));
    }
    
    @GetMapping("/impresoras")
    @Operation(summary = "Listar impresoras disponibles")
    public Mono<List<String>> listarImpresorasDisponibles() {
        return impresionService.listarImpresorasDisponibles();
    }
}
