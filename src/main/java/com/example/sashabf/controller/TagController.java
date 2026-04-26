package com.example.sashabf.controller;

import com.example.sashabf.model.Tag;
import com.example.sashabf.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // POST: /api/tags -> Creación colaborativa (USER y ADMIN)
    @PostMapping
    public ResponseEntity<Tag> create(@RequestBody Tag tag) {
        // Extraemos el nombre del objeto enviado para usar nuestro método de servicio
        Tag newTag = tagService.createTag(tag.getName());
        return new ResponseEntity<>(newTag, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Tag>> getAll() {
        // Llamamos al servicio para obtener todas las etiquetas de la BD
        List<Tag> tags = tagService.getAllTags();
        
        // Devolvemos la lista con un código 200 (OK)
        return ResponseEntity.ok(tags);
    }
}
