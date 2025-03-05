package com.mx.atendi.entity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@Document(collection = "contador_turnos")
public class ContadorTurnos {
    
    @Id
    private String id; // 🔥 MongoDB generará este ID automáticamente.
    private String hospitalId;
    private String departamentoId;
    private LocalDate fecha;
    private String ultimoTurno; // 🔥 Último turno generado (ej: "A099")
}