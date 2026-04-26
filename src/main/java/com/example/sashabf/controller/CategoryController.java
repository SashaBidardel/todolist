package com.example.sashabf.controller;

import com.example.sashabf.model.Category;
import com.example.sashabf.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

   @Autowired
	private CategoryService categoryService;

   

    // GET: /api/categories -> Todos pueden verlas para elegir una
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // POST: /api/categories -> Solo ADMIN (configurado en SecurityConfig)
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
    }

    // DELETE: /api/categories/{id} -> Solo ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}