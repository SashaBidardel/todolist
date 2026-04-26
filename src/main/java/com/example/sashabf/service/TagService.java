package com.example.sashabf.service;

import com.example.sashabf.model.Tag;
import com.example.sashabf.model.Task;
import com.example.sashabf.repository.TagRepository;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class TagService {

    @Autowired
	private TagRepository tagRepository;

   

    public Tag createTag(String name) {
        String cleanName = name.trim().toLowerCase();
        
        // Comprobamos si ya existe
        if (tagRepository.findByName(cleanName).isPresent()) {
            throw new RuntimeException("La etiqueta '" + cleanName + "' ya existe y es de uso global.");
        }
        
        Tag tag = new Tag();
        tag.setName(name);      
        tag.setTasks(new ArrayList<>());      
        return tagRepository.save(tag);
    }
    public void addTaskToTag(Long tagId, Task task) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag no encontrado"));
        
        // Añadimos la tarea a la colección del Tag
        tag.getTasks().add(task);
        
        //  En relaciones ManyToMany bidireccionales, 
        // también añadimos el tag a la tarea para que JPA lo guarde bien
        task.getTags().add(tag);
        
        tagRepository.save(tag);
    }
  // LISTAR TODAS: Recupera todas las etiquetas de la base de datos
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
}
