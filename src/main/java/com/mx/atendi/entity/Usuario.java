package com.mx.atendi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Representa un usuario del sistema.
 * Cada usuario debe pertenecer a un hospital (hospitalId se asigna según el Hospital existente).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;
    private String hospitalId; // ID del hospital al que pertenece el usuario (debe ser el mismo para todos los usuarios del mismo hospital)
    private String nombre;
    private String userName;   // Nombre de usuario para autenticación.
    private String password;   // Contraseña para autenticación.
    private String rol; // Ej: "administrador", "recepcion", "ventanilla", "monitor"
    private String departamentoId; // ID del departamento asignado
    private String ventanillaId; // ID de la ventanilla
    private Integer numeroVentanilla; // Nuevo campo
}

