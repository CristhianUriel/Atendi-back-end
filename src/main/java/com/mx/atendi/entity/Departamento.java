package com.mx.atendi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "departamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Departamento {

    @Id
    private String id;
    private String nombre;
    private List<String> ventanillasIds; // 🔗 Ahora almacena IDs de ventanillas en lugar de números fijos
    private List<String> operacionesIds; // 🔗 Lista de IDs de operaciones permitidas en este departamento
}

