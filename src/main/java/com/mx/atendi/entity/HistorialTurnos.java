package com.mx.atendi.entity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "historial_turnos")
public class HistorialTurnos {

    @Id
    private String id;
    private String numeroTurno;
    private String hospitalId;
    private String departamentoId;
    private String tipoOperacion;
    private String estado;
    private String atendidoPor;
    private LocalDateTime horaCreacion;
    private LocalDateTime horaAtencion;
    private LocalDateTime horaFinalizacion;
}

