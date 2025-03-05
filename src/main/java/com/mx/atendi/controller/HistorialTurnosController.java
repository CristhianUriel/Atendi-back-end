package com.mx.atendi.controller;

import com.mx.atendi.entity.HistorialTurnos;
import com.mx.atendi.service.IHistorialTurnosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Gestión de reportes históricos de turnos")
public class HistorialTurnosController {

    private final IHistorialTurnosService historialTurnosService;

    public HistorialTurnosController(IHistorialTurnosService historialTurnosService) {
        this.historialTurnosService = historialTurnosService;
    }

    @GetMapping("/fecha")
    @Operation(summary = "Obtener turnos por fecha", description = "Devuelve los turnos atendidos en un rango de fechas")
    public Flux<HistorialTurnos> obtenerTurnosPorFecha(@RequestParam String hospitalId,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return historialTurnosService.obtenerTurnosPorFecha(hospitalId, inicio, fin);
    }

    @GetMapping("/departamento")
    @Operation(summary = "Obtener turnos por departamento", description = "Devuelve los turnos atendidos por departamento")
    public Flux<HistorialTurnos> obtenerTurnosPorDepartamento(@RequestParam String hospitalId,
                                                              @RequestParam String departamentoId) {
        return historialTurnosService.obtenerTurnosPorDepartamento(hospitalId, departamentoId);
    }

    @GetMapping("/usuario")
    @Operation(summary = "Obtener turnos por usuario", description = "Devuelve los turnos atendidos por un usuario")
    public Flux<HistorialTurnos> obtenerTurnosPorUsuario(@RequestParam String usuarioId) {
        return historialTurnosService.obtenerTurnosPorUsuario(usuarioId);
    }

    @GetMapping("/tipo-operacion")
    @Operation(summary = "Obtener turnos por tipo de operación", description = "Devuelve los turnos atendidos de un tipo de operación")
    public Flux<HistorialTurnos> obtenerTurnosPorTipoOperacion(@RequestParam String hospitalId,
                                                               @RequestParam String tipoOperacion) {
        return historialTurnosService.obtenerTurnosPorTipoOperacion(hospitalId, tipoOperacion);
    }
}
