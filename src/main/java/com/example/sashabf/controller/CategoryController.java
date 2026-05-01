package com.example.sashabf.controller;

import com.example.sashabf.model.Category;
import com.example.sashabf.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorías", description = "Gestión de categorías para las tareas (Solo personal autorizado)")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 1. GET: Ver categorías
    @Operation(
        summary = "Listar todas las categorías",
        description = "Permite ver todas las categorías disponibles. Requiere estar autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
        })
    @PreAuthorize("isAuthenticated()") 
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // 2. POST: Crear categoría
    @Operation(
        summary = "Crear nueva categoría",
        description = "Añade una categoría al sistema. Solo permitido para ADMIN o GESTOR."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de la categoría inválidos"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos (requiere ADMIN o GESTOR)")
        })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
    }

    // 3. DELETE: Borrar categoría
    @Operation(
        summary = "Eliminar categoría",
        description = "Borra una categoría por su ID. Solo permitido para ADMIN o GESTOR."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoría eliminada con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos"),
            @ApiResponse(responseCode = "404", description = "ID de categoría no encontrado")
        })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    // 4. PUT: Editar categoría
    @Operation(
        summary = "Actualizar categoría",
        description = "Modifica el título de una categoría existente. Solo permitido para ADMIN o GESTOR."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos"),
            @ApiResponse(responseCode = "404", description = "La categoría no existe")
        })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category category) {
        Category updatedCategory = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updatedCategory);
    }
}