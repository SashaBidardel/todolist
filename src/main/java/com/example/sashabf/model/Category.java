package com.example.sashabf.model;



import jakarta.persistence.*;
import lombok.*;
import java.util.List;

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
    private String title; 

    @OneToMany(mappedBy = "category")
    private List<Task> tasks; 
}
