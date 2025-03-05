package com.mx.atendi.service;

import com.mx.atendi.entity.HistorialTurnos;
import reactor.core.publisher.Flux;
import java.time.LocalDate;

public interface IHistorialTurnosService {

    /**
     * Obtiene los turnos atendidos en un rango de fechas para un hospital.
     *
     * @param hospitalId ID del hospital.
     * @param inicio Fecha de inicio.
     * @param fin Fecha de fin.
     * @return Flux<HistorialTurnos> con los turnos atendidos en el rango de fechas.
     */
    Flux<HistorialTurnos> obtenerTurnosPorFecha(String hospitalId, LocalDate inicio, LocalDate fin);

    /**
     * Obtiene los turnos atendidos por un departamento.
     *
     * @param hospitalId ID del hospital.
     * @param departamentoId ID del departamento.
     * @return Flux<HistorialTurnos> con los turnos atendidos por el departamento.
     */
    Flux<HistorialTurnos> obtenerTurnosPorDepartamento(String hospitalId, String departamentoId);

    /**
     * Obtiene los turnos atendidos por un usuario específico.
     *
     * @param usuarioId ID del usuario.
     * @return Flux<HistorialTurnos> con los turnos atendidos por el usuario.
     */
    Flux<HistorialTurnos> obtenerTurnosPorUsuario(String usuarioId);

    /**
     * Obtiene los turnos atendidos de un tipo de operación específico.
     *
     * @param hospitalId ID del hospital.
     * @param tipoOperacion Tipo de operación.
     * @return Flux<HistorialTurnos> con los turnos atendidos del tipo de operación.
     */
    Flux<HistorialTurnos> obtenerTurnosPorTipoOperacion(String hospitalId, String tipoOperacion);
}
