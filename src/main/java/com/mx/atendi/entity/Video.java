package com.mx.atendi.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "videos")
public class Video {
    @Id
    private String id;
    private String nombre;
    private String path;
    private LocalDateTime fechaSubida;
}
