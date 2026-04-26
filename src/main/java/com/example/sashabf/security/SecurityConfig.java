package com.example.sashabf.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Deshabilitar CSRF (necesario para APIs que usan Postman/Stateless)
            .csrf(csrf -> csrf.disable())

            // 2. Definir reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // Registro de usuarios: Abierto para todo el mundo
                .requestMatchers("/api/users/register").permitAll()

                // Categorías: Todos las ven (GET), solo ADMIN las crea/borra (POST/DELETE)
                .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                .requestMatchers("/api/categories/**").hasAuthority("ADMIN")

                // Tags: Creación colaborativa para cualquier usuario registrado
                .requestMatchers("/api/tags/**").authenticated()

                // Tareas: Cualquier usuario registrado (el filtro de dueño/admin va en el Service)
                .requestMatchers("/api/tasks/**").authenticated()

                // Cualquier otra ruta requiere estar logueado
                .anyRequest().authenticated()
            )

            // 3. Tipo de autenticación: Básica (Usuario y contraseña en las cabeceras)
            .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
