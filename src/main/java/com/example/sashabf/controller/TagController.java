package com.example.sashabf.controller;

import com.example.sashabf.exception.ForbiddenException;
import com.example.sashabf.model.Tag;
import com.example.sashabf.model.Task;
import com.example.sashabf.model.User;
import com.example.sashabf.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Etiquetas", description = "Gestión de etiquetas colaborativas")//hay colisión de nombres con la clase
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // 1. POST: Crear Tag
    @Operation(
        summary = "Crear nueva etiqueta",
        description = "Añade una etiqueta al sistema. Disponible para usuarios registrados y administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Etiqueta creada con éxito"),
            @ApiResponse(responseCode = "400", description = "El nombre de la etiqueta no es válido"),
            @ApiResponse(responseCode = "401", description = "No autorizado: Debes iniciar sesión")
        })
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<Tag> create(@RequestBody Tag tag) {
        Tag newTag = tagService.createTag(tag.getName());
        return new ResponseEntity<>(newTag, HttpStatus.CREATED);
    }

    // 2. GET: Listar todos
    @Operation(
        summary = "Listar todas las etiquetas",
        description = "Recupera todas las etiquetas disponibles en la base de datos."
    )
    
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
        })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<Tag>> getAll() {
        return ResponseEntity.ok(tagService.getAllTags());
    }
    
    // 3. PUT: Editar Tag
    @Operation(
        summary = "Actualizar etiqueta",
        description = "Modifica una etiqueta existente. El servicio validará si el usuario tiene permiso."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etiqueta actualizada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido: No tienes permiso para editar esta etiqueta"),
            @ApiResponse(responseCode = "404", description = "La etiqueta no existe")
        })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<Tag> update(
        @PathVariable Long id, 
        @RequestBody Tag tag, 
        @AuthenticationPrincipal User user
    ) throws ForbiddenException {
        Tag updatedTag = tagService.updateTag(id, tag, user);
        return ResponseEntity.ok(updatedTag);
    }

    // 4. DELETE: Borrar Tag
    @Operation(
        summary = "Eliminar etiqueta",
        description = "Borra una etiqueta por su ID. Solo permitido si el usuario es dueño o tiene rango superior."
    )@ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Etiqueta eliminada con éxito"),
            @ApiResponse(responseCode = "403", description = "Prohibido: No eres el dueño de esta etiqueta"),
            @ApiResponse(responseCode = "404", description = "Etiqueta no encontrada")
        })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable Long id, 
        @AuthenticationPrincipal User user
    ) throws ForbiddenException {
        tagService.deleteTag(id, user);
        return ResponseEntity.noContent().build();
    }
    
    // 5. GET2: Tareas por Tag
    @Operation(
        summary = "Listar tareas por etiqueta",
        description = "Busca todas las tareas asociadas a una etiqueta específica mediante su nombre."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tareas recuperada con éxito"),
            @ApiResponse(responseCode = "401", description = "No autorizado: Debes iniciar sesión"),
            @ApiResponse(responseCode = "404", description = "No se encontró ninguna etiqueta con ese nombre")
        })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{name}/tasks")
    public ResponseEntity<List<Task>> getTasksByTag(@PathVariable String name) {
        List<Task> tasks = tagService.getTasksByTagName(name);
        return ResponseEntity.ok(tasks);
    }
}