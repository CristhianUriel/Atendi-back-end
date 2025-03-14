package com.mx.atendi.dto;

import java.time.LocalDateTime;

import com.mx.atendi.entity.Turno;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoDTO {
	private String id; // 🔥 ID de MongoDB (NO lo usaremos como número de turno)
	private String numeroTurno; // 🔥 Turno alfanumérico (A001, A002, ..., Z999, AA001)
	private String hospitalId; // Referencia al ID único del hospital (obtenido del documento Hospital)
    private String departamentoId;
    private String tipoOperacionId;
    private String tipoOperacionNombre;
    private String atendidoPor; // 🔥 Usuario que atendió el turno
    private String estado; // pendiente, en proceso, atendido, no atendido
	private LocalDateTime horaCreacion; // Fecha y hora de creación
	private LocalDateTime horaAtencion; // Fecha y hora de la última actualización
}
