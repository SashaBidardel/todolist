package com.example.sashabf.controller; 
import com.example.sashabf.model.User;
import com.example.sashabf.model.UserRole;
import com.example.sashabf.service.UserService; // Ajusta al nombre de tu servicio

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth-Contoller", description = "Registro de nuevos usuarios.")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Operation(
            summary = "Registrar nuevo usuario", 
            description = "Crea un usuario en el sistema con el rol especificado."
        )
        @ApiResponses(value = {
            @ApiResponse(
                responseCode = "201", 
                description = "Usuario registrado con éxito",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = User.class),
                    examples = @ExampleObject(
                        name = "Ejemplo de Respuesta 201",
                        value = "{\n" +
                                "  \"username\": \"sttetring\",\n" +
                                "  \"password\": \"$2a$10$MabV.ROLeBj1BftsP7/GqOwW7n/zvRHGg/w97qHiuTpvmBd9DZhXi\",\n" +
                                "  \"email\": \"string\",\n" +
                                "  \"fullname\": \"string\",\n" +
                                "  \"role\": \"USER\"\n" +
                                "}"
                    )
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
        })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
       

        // Validación de seguridad para que no rompa el PasswordEncoder en el servicio
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error: La contraseña no puede estar vacía en la petición.");
        }

        // Asignamos un rol por defecto si no viene en el JSON
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }

        // Enviamos el usuario al servicio (allí se encriptará la contraseña y se guardará)
        User nuevoUsuario = userService.registerUser(user);
        
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }
}