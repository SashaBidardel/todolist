package com.example.sashabf.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                
                // 1. ACCESO LIBRE: Documentación y Registro
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/api/auth/**"
                ).permitAll()

                // 2. ZONA ADMIN (/api/admin/**): Solo para el rol ADMIN
                // Bloqueamos todo el prefijo de golpe por seguridad
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                // 3. ZONA GESTOR (/api/manager/**): Accesible para GESTOR y ADMIN
                // Aquí es donde viven los endpoints de creación/edición de categorías
                .requestMatchers("/api/manager/**").hasAnyAuthority("GESTOR", "ADMIN")

             // 4. ZONA USUARIO Y DATOS COMUNES: Autenticados (USER, GESTOR, ADMIN)
                // - El GET de categorías para que todos puedan usarlas
                .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                // - Gestión de tareas y etiquetas propias
                .requestMatchers("/api/tasks/**").authenticated()
                .requestMatchers("/api/tags/**").authenticated()
                // - Gestión del perfil propio (Ver y Modificar)
                .requestMatchers("/api/user/profile").authenticated() // <--- NUEVA RUTA DEL PUT
                .requestMatchers("/api/users/me").authenticated()

                // 5. CIERRE DE SEGURIDAD
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}