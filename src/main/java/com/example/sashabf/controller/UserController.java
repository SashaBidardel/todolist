package com.example.sashabf.controller;

import com.example.sashabf.model.User;
import com.example.sashabf.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Endpoints para la gestión de cuentas y perfiles")
public class UserController {

    @Autowired   
	 private UserService userService;

   
    	// 1. Registro público
        @Operation(
            summary = "Registro de nuevo usuario",
            description = "Permite a cualquier persona crear una cuenta. Por defecto se asigna el rol USER.",
            security = { } // Sin candado en Swagger
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
        })
        @PostMapping("/register")
        public ResponseEntity<User> register(@RequestBody User user) {
            User newUser = userService.registerUser(user);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        }

        // 2. Listado (Solo ADMIN)
        @Operation(
            summary = "Listar todos los usuarios",
            description = "Devuelve una lista completa de usuarios. Requiere rol ADMIN."
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Listado de usuarios obtenido correctamente"),
                @ApiResponse(responseCode = "401", description = "No autenticado (falta login)"),
                @ApiResponse(responseCode = "403", description = "No tienes permisos (requiere rol ADMIN)")
            })
        @PreAuthorize("hasAuthority('ADMIN')")
        @GetMapping
        public ResponseEntity<List<User>> getAll() {
            return ResponseEntity.ok(userService.getAllUsers());
        }

        // 3. Eliminar (Solo ADMIN)
        @Operation(
            summary = "Eliminar un usuario",
            description = "Borra físicamente al usuario de la base de datos por su ID. Requiere rol ADMIN."
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "Usuario eliminado con éxito (Sin contenido en la respuesta)"),
                @ApiResponse(responseCode = "401", description = "No autorizado: Debes estar autenticado para borrar"),
                @ApiResponse(responseCode = "403", description = "Prohibido: Solo los administradores tienen permisos para esta acción"),
                @ApiResponse(responseCode = "404", description = "No encontrado: El ID de usuario proporcionado no existe")
            })
        @PreAuthorize("hasAuthority('ADMIN')")
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
        @Parameter(description = "ID del usuario a eliminar", required = true, example = "5")
        @PathVariable Long id) {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        }
        
        // 4. Editar 
        @Operation(
            summary = "Actualizar perfil de usuario",
            description = "Permite modificar los datos de un usuario existente. Requiere rol ADMIN o ser el usuario en cuestión."
        )
        @ApiResponses(value = {
        		@ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
                @ApiResponse(responseCode = "401", description = "No autenticado"),
                @ApiResponse(responseCode = "403", description = "Prohibido: Solo un ADMIN puede borrar usuarios"),
                @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            })
       
        @PutMapping("/{id}")
        public ResponseEntity<User> update(@Parameter(description = "ID del usuario a modificar", required = true, example = "2")
            @PathVariable Long id, 
            @RequestBody User user, 
            @AuthenticationPrincipal UserDetails currentUserDetails
        ) {
        	User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        }

       
    
    @Operation(
            summary = "Promocionar a Gestor", 
            description = "Cambia el rol de un usuario normal a Gestor utilizando su ID único."
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "El usuario con ese ID no existe"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para realizar esta acción")
        })
        @PatchMapping("/{id}/promote")
        public ResponseEntity<Map<String, String>> promote(
        @Parameter(description = "ID del usuario a promocionar", required = true, example = "3")
        @PathVariable Long id) {
            userService.promoteToGestor(id);
            return ResponseEntity.ok(Map.of("message", "Usuario promocionado a GESTOR con éxito"));
        }

        @Operation(summary = "Degradar a Usuario", description = "Quita los permisos de Gestor a un usuario y lo devuelve al rol USER.")
        @ApiResponse(responseCode = "200", description = "Rol revocado correctamente")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente"),
                @ApiResponse(responseCode = "404", description = "El usuario con ese ID no existe"),
                @ApiResponse(responseCode = "403", description = "No tienes permisos para realizar esta acción")
            })
        @PatchMapping("/{id}/demote")
        public ResponseEntity<Map<String, String>> demote(
        @Parameter(description = "ID del usuario a degradar", required = true, example = "3") 
         @PathVariable Long id) {
            userService.demoteToUser(id);
            return ResponseEntity.ok(Map.of("message", "Usuario degradado a USER con éxito"));
        }
}
