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
	private String id; // Identificador con nomenclatura: OP-NNN (ej. "RE-001")
	private String hospitalId; // Referencia al ID único del hospital (obtenido del documento Hospital)
	private String usuario; // Usuario que genera el turno
	private String servicio; // Servicio asociado al turno
	private List<String> tipoOperacion; // Ej: ["registro", "pago", "consulta"]
	private String estado; // "pendiente", "tomado", "atendido", "no-atendido"
	private String ventanilla; // Ventanilla asignada (si aplica)
	private LocalDateTime horaCreacion; // Fecha y hora de creación
	private LocalDateTime horaActualizacion; // Fecha y hora de la última actualización
}
