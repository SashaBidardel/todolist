package com.example.sashabf.controller;

import com.example.sashabf.exception.BadRequestException;
import com.example.sashabf.exception.ForbiddenException;
import com.example.sashabf.model.Tag;
import com.example.sashabf.model.Task;
import com.example.sashabf.model.User;
import com.example.sashabf.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
// Resolvemos la colisión usando la ruta completa aquí para mantener los imports limpios
@io.swagger.v3.oas.annotations.tags.Tag(name = "Etiquetas", description = "Gestión de etiquetas personales para tareas")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // 1. POST: Crear Tag
    @Operation(
        summary = "Crear nueva etiqueta",
        description = "Añade una etiqueta privada al catálogo del usuario. Solo disponible para USER y GESTOR."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Etiqueta creada con éxito"),
            @ApiResponse(responseCode = "400", description = "Nombre inválido o duplicado"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Los ADMIN no pueden crear etiquetas")
        })
    @PreAuthorize("hasAnyAuthority('USER', 'GESTOR')") // Coherente con el Service
    @PostMapping
    public ResponseEntity<Tag> create(@RequestBody Tag tag) {
        String username = getAuthenticatedUsername();
        Tag createdTag = tagService.createTag(tag, username);
        return new ResponseEntity<>(createdTag, HttpStatus.CREATED);
    }

    // 2. GET: Listar etiquetas del usuario
    @Operation(
        summary = "Listar mis etiquetas",
        description = "Recupera las etiquetas creadas por el usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
        })
    @PreAuthorize("hasAnyAuthority('USER', 'GESTOR')")
    @GetMapping
    public ResponseEntity<List<Tag>> getMyTags() {
        List<Tag> tags = tagService.getUserTags(getAuthenticatedUsername());
        return ResponseEntity.ok(tags);
    }

    // 3. PUT: Editar Tag
    @Operation(summary = "Actualizar etiqueta", description = "Modifica una etiqueta si eres el autor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actualizada"),
            @ApiResponse(responseCode = "403", description = "No eres el autor"),
            @ApiResponse(responseCode = "404", description = "No existe")
        })
    @PreAuthorize("hasAnyAuthority('USER', 'GESTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Tag> update(@PathVariable Long id, @RequestBody Tag tag) {
        Tag updated = tagService.updateTag(id, tag, getAuthenticatedUsername());
        return ResponseEntity.ok(updated);
    }

    // 4. DELETE: Borrar Tag
    @Operation(summary = "Eliminar etiqueta", description = "Borra una etiqueta si eres el autor.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'GESTOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.deleteTag(id, getAuthenticatedUsername());
        return ResponseEntity.noContent().build();
    }

    // 5. Tareas por etiqueta
    @Operation(summary = "Listar tareas por etiqueta", description = "Busca tareas propias filtrando por nombre de etiqueta.")
    @GetMapping("/tasks/{tagName}")
    @PreAuthorize("hasAnyAuthority('USER', 'GESTOR')")
    public ResponseEntity<List<Task>> getTasksByTag(
            @Parameter(description = "Nombre de la etiqueta", example = "Trabajo")
            @PathVariable String tagName) {
        
        List<Task> tasks = tagService.getTasksByTagName(tagName, getAuthenticatedUsername());
        return ResponseEntity.ok(tasks);
    }

    // MÉTODO PRIVADO PARA LIMPIAR EL CÓDIGO
    private String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}