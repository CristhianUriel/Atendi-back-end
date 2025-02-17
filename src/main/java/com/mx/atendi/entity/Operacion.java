package com.mx.atendi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "operaciones")
@Data
public class Operacion {

    @Id
    private String id;
    private String nombre; // Ejemplo: "Consulta médica", "Pago de factura", etc.
}