package com.mx.atendi.service;

import com.mx.atendi.entity.HistorialTurnos;
import com.mx.atendi.repository.HistorialTurnosRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Service
@Slf4j
public class HistorialTurnosService implements IHistorialTurnosService {

    private final HistorialTurnosRepository historialTurnosRepository;

    public HistorialTurnosService(HistorialTurnosRepository historialTurnosRepository) {
        this.historialTurnosRepository = historialTurnosRepository;
    }

    @Override
    public Flux<HistorialTurnos> obtenerTurnosPorFecha(String hospitalId, LocalDate inicio, LocalDate fin) {
        LocalDateTime inicioFecha = inicio.atStartOfDay();
        LocalDateTime finFecha = fin.plusDays(1).atStartOfDay();

        return historialTurnosRepository.findAll()
                .filter(turno -> turno.getHospitalId().equals(hospitalId)
                        && turno.getHoraFinalizacion().isAfter(inicioFecha)
                        && turno.getHoraFinalizacion().isBefore(finFecha));
    }

    @Override
    public Flux<HistorialTurnos> obtenerTurnosPorDepartamento(String hospitalId, String departamentoId) {
        return historialTurnosRepository.findAll()
                .filter(turno -> turno.getHospitalId().equals(hospitalId)
                        && turno.getDepartamentoId().equals(departamentoId));
    }

    @Override
    public Flux<HistorialTurnos> obtenerTurnosPorUsuario(String usuarioId) {
        return historialTurnosRepository.findAll()
                .filter(turno -> turno.getAtendidoPor().equals(usuarioId));
    }

    @Override
    public Flux<HistorialTurnos> obtenerTurnosPorTipoOperacion(String hospitalId, String tipoOperacion) {
        return historialTurnosRepository.findAll()
                .filter(turno -> turno.getHospitalId().equals(hospitalId)
                        && turno.getTipoOperacion().equalsIgnoreCase(tipoOperacion));
    }
}
