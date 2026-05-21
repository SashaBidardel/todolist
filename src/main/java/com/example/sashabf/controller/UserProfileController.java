package com.example.sashabf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody; // <-- CAMBIADO: Import de Spring correcto
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sashabf.DTO.UpdateProfileDTO;
import com.example.sashabf.model.User;
import com.example.sashabf.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Perfil de Usuario",description = "Edición del perfil por el propio usuario.")
public class UserProfileController {

    @Autowired 
    private UserService userService;

    @Operation(
        summary = "Actualizar perfil de usuario",
        description = "Permite modificar los datos del propio usuario autenticado (username, email, password, fullname)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Prohibido: No tienes los permisos necesarios"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
        @RequestBody UpdateProfileDTO profileDto, // <-- Recibe el DTO
        @AuthenticationPrincipal UserDetails currentUserDetails
    ) {
        // El controlador solo extrae el username  y se lo pasa todo al servicio
        String usernameActual = currentUserDetails.getUsername();
        
        // Pasamos 'profileDto' al servicio
        User updatedUser = userService.updateUserByUsername(usernameActual, profileDto);
        
        return ResponseEntity.ok(updatedUser);
    }
}
