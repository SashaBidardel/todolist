package com.example.sashabf.model;



import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tags")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author; // El creador del tag

    @Column(nullable = false, unique = true)
    private String name;

    @JsonIgnoreProperties("tags")
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private List<Task> tasks = new ArrayList<>(); 
}
