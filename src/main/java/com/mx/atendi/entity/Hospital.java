package com.mx.atendi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Representa un hospital en el sistema.
 * El campo 'id' se genera automáticamente (por ejemplo, como ObjectId de MongoDB).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "hospitales")
public class Hospital {
    @Id
    private String id; // Identificador único del hospital
    private String nombre; // Nombre del hospital
}

