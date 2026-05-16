package com.example.sashabf.controller;

import com.example.sashabf.model.Category;
import com.example.sashabf.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/categories")
@Tag(name = "Categorías - Gestor", description = "Operaciones de creación y edición para GESTOR o ADMIN")
@PreAuthorize("hasAnyAuthority('GESTOR', 'ADMIN')")
public class GestorCategoryController {

	@Autowired
    private CategoryService categoryService;

    @Operation(
        summary = "Listar categorías (Vista Gestor/Admin)", 
        description = "Permite obtener el listado completo de categorías para tareas de gestión operativa. Accesible para usuarios con rol GESTOR o ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado: Requiere autenticación"),
        @ApiResponse(responseCode = "403", description = "Prohibido: Se requiere rol GESTOR o ADMIN")
    })
    // Permitimos explícitamente ambos roles mediante hasAnyAuthority
    @PreAuthorize("hasAnyAuthority('GESTOR', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Crear categoría", description = "Añade una nueva categoría al catálogo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada con éxito"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "403", description = "Prohibido: Solo GESTOR o ADMIN")
    })
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar categoría")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente"),
        @ApiResponse(responseCode = "403", description = "Permisos insuficientes"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(
        @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id, 
        @RequestBody Category category) {
        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }
    @Operation(
            summary = "Eliminar categoría definitivamente", 
            description = "Acción irreversible reservada exclusivamente para el rol ADMIN."
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoría eliminada con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes privilegios de ADMIN"),
            @ApiResponse(responseCode = "404", description = "La categoría con ese ID no existe")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la categoría a borrar", example = "5") @PathVariable Long id) {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        }
}
