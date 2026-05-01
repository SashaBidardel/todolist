package com.example.sashabf.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            
	        	// 1. Registro: Abierto
	        	.requestMatchers( "/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
	        	.requestMatchers("/api/users/register").permitAll()

	            // 2. Usuarios: Solo el ADMIN puede gestionar el listado y los roles
	            .requestMatchers(HttpMethod.GET, "/api/users").hasAuthority("ADMIN") 
	            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ADMIN")
	            //  ADMIN promocione/degrade (ej: /api/users/role/{id})
	            .requestMatchers(HttpMethod.PATCH, "/api/users/*/promote", "/api/users/*/demote").hasAuthority("ADMIN")

	         // 3. Categorías
	         // Primero: Definimos quién puede ver (GET). Esto incluye a USER, ADMIN y GESTOR.
	         .requestMatchers(HttpMethod.GET, "/api/categories/**", "/api/categories").authenticated()

	         // Segundo: Cualquier otro método (POST, PUT, DELETE) sobre categorías
	         // SOLO se permite a ADMIN y GESTOR.
	         .requestMatchers("/api/categories/**", "/api/categories").hasAnyAuthority("ADMIN", "GESTOR")
	            // 4. Tags: el USUARIO puede hacer CRUD de sus Tags
	          
	            .requestMatchers("/api/tags/**").hasAnyAuthority("USER", "GESTOR")

	            // 5. Tasks: el USUARIO puede hacer CRUD de sus Tags
	            .requestMatchers("/api/tasks/**").hasAnyAuthority("USER", "GESTOR")
	            // Permitimos PUT a /api/users/{id} para que el USER edite su perfil
	            .requestMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated()
	            
	            .anyRequest().authenticated()
	        )
	        .httpBasic(withDefaults());

	    return http.build();
	}

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
