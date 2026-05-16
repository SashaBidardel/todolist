package com.example.sashabf.model;



import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id; 

    @Column(nullable = false, unique = true)
    @Schema(example = "General", description = "Título de la categoría")
    private String title; 
    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<Task> tasks; 
}
