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
                
                // 1. SWAGGER Y REGISTRO: Siempre lo primero y totalmente abierto
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/swagger-resources/**"
                ).permitAll()
                .requestMatchers("/api/users/register").permitAll()

                // 2. USUARIOS (ADMIN): Listado, borrado y roles
                .requestMatchers(HttpMethod.GET, "/api/users").hasAuthority("ADMIN") 
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyAuthority("ADMIN", "USER")
                //.requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/users/*/promote", "/api/users/*/demote").hasAuthority("ADMIN")

                // 3. CATEGORÍAS: 
                // Lectura permitida a todos los autenticados (USER, GESTOR, ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                // Escritura (POST, PUT, DELETE) solo ADMIN y GESTOR
                .requestMatchers("/api/categories/**").hasAnyAuthority("ADMIN", "GESTOR")

                // 4. TAGS Y TASKS: El USER y GESTOR pueden hacer CRUD
                
                .requestMatchers("/api/tags/**").hasAnyAuthority("USER", "GESTOR")
                .requestMatchers("/api/tasks/**").hasAnyAuthority("USER", "GESTOR")

                
                // 5. CIERRE
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
