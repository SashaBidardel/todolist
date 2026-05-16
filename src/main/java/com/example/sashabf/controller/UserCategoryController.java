package com.example.sashabf.controller;

import com.example.sashabf.model.Category;
import com.example.sashabf.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorías - Usuario", description = "Endpoints de solo lectura para usuarios finales")
public class UserCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(
        summary = "Listar categorías (Modo lectura)", 
        description = "Permite ver las categorías disponibles para asignar a tareas. Accesible para cualquier usuario logueado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista recuperada con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado: Requiere iniciar sesión")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}
