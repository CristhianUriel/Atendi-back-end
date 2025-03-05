package com.mx.atendi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa un turno en el sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "turnos")
public class Turno {
	@Id
	private String id; // 🔥 ID de MongoDB (NO lo usaremos como número de turno)
	private String numeroTurno; // 🔥 Turno alfanumérico (A001, A002, ..., Z999, AA001)
	private String hospitalId; // Referencia al ID único del hospital (obtenido del documento Hospital)
    private String departamentoId;
    private String tipoOperacion;
    private String atendidoPor; // 🔥 Usuario que atendió el turno
    private String estado; // pendiente, en proceso, atendido, no atendido
	private LocalDateTime horaCreacion; // Fecha y hora de creación
	private LocalDateTime horaAtencion; // Fecha y hora de la última actualización

}
