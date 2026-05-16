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
@RequestMapping("/api/admin/categories")
@Tag(name = "Categorías-Admin", description = "Gestión administrativa total. Solo accesible para ADMIN.")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "Listar todas las categorías (Vista Administrador)")
    @ApiResponse(responseCode = "200", description = "Éxito")
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Crear categoría (Admin)")
    @ApiResponse(responseCode = "201", description = "Creado")
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar categoría (Admin)")
    @ApiResponse(responseCode = "200", description = "Actualizado")
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category category) {
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
