package com.example.sashabf.model;



import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "tags")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Tag {

    @Id @Schema(accessMode = Schema.AccessMode.READ_ONLY) // El ID no se envía, se genera
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 
    
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) // El autor no se envía, se saca de la sesión
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author; // El creador del tag
    
    @Schema(example = "Nombre Etiqueta")
    @Column(nullable = false, unique = true)
    private String name;
    
    
    @JsonIgnoreProperties("tags")
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private List<Task> tasks = new ArrayList<>(); 
}
