package com.example.sashabf.model; // Ajusta el paquete si es necesario

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data // Genera automáticamente getters, setters, equals, hashCode y toString
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class User {

    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "fullname")
    private String fullname;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, hidden = true)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; 
}

