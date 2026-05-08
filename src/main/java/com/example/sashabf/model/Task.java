package com.example.sashabf.model;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "tasks")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor

public class Task {

    @Id  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean completed = false;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    //ATRIBUTOS EXTRA 
    private LocalDate deadline; // Fecha límite 
    
    @Enumerated(EnumType.STRING)
    private Priority priority; // Nivel de prioridad 
    
    private String estimatedTime; // Ejemplo: "2 horas", "3 días"

    @Column(nullable = false)
    private boolean important = false; // Por defecto no es destacada
    // -------------------------------------
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User author; // Relación 1 a muchos con User 
    
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("tasks")
    private Category category; // Relación 1 a muchos con Category

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "task_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new java.util.ArrayList<>(); // Relación muchos a muchos con Tag 

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }
}
