package com.mx.atendi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ventanillas_catalogo")
@Data
public class VentanillaCatalogo {

    @Id
    private String id;
    private String nombre; // Ejemplo: "Ventanilla de Caja", "Ventanilla de Atención"
    private boolean activo; // 🔥 Estado para activar/desactivar la ventanilla
}
