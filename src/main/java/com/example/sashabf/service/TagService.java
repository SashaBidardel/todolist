package com.example.sashabf.service;

import com.example.sashabf.exception.BadRequestException;
import com.example.sashabf.exception.ForbiddenException;
import com.example.sashabf.exception.ResourceNotFoundException;
import com.example.sashabf.model.Tag;
import com.example.sashabf.model.Task;
import com.example.sashabf.model.User;
import com.example.sashabf.model.UserRole;
import com.example.sashabf.repository.TagRepository;
import com.example.sashabf.repository.TaskRepository;
import com.example.sashabf.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service

public class TagService {

    @Autowired
	private TagRepository tagRepository;
    @Autowired
	private UserRepository userRepository;
    @Autowired
   	private TaskRepository taskRepository;
   
    //Crear TAG
    public Tag createTag(Tag tagDetails, String username) throws ForbiddenException {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Bloqueamos explícitamente al ADMIN
        if (author.getRole() == UserRole.ADMIN) {
            throw new ForbiddenException("El administrador no puede tener etiquetas personales.");
        }

        // Validación de datos
        if (tagDetails.getName() == null || tagDetails.getName().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la etiqueta es obligatorio.");
        }

        // Evitar duplicados para este usuario específico
        tagRepository.findByNameAndAuthor(tagDetails.getName(), author)
                .ifPresent(existing -> {
                    throw new BadRequestException("Ya tienes una etiqueta llamada: " + tagDetails.getName());
                });

        Tag newTag = new Tag();
        newTag.setName(tagDetails.getName());
        newTag.setAuthor(author);
        
        return tagRepository.save(newTag);
    }
    public void addTaskToTag(Long tagId, Task task) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));
        
        // Añadimos la tarea a la colección del Tag
        tag.getTasks().add(task);
        
        //  En relaciones ManyToMany bidireccionales, 
        // también añadimos el tag a la tarea para que JPA lo guarde bien
        task.getTags().add(tag);
        
        tagRepository.save(tag);
    }
    //2.  LISTAR TODAS: Recupera  las etiquetas del autor de la base de datos
    public List<Tag> getUserTags(String username) {
        // 1. Buscamos al objeto User completo para poder filtrar por él
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Usamos el método del repositorio que busca por el campo 'author'
        return tagRepository.findByAuthor(author);
    }
    
    //3. ACTUALIZAR TAG
    public Tag updateTag(Long id, Tag tagDetails, String username) throws ForbiddenException {
        // 1. Validaciones básicas de negocio
        if (tagDetails.getName() == null || tagDetails.getName().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la etiqueta es obligatorio");
        }

        // 2. Recuperar la etiqueta original
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));

        // 3. Verificar propiedad
        if (!tag.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenException("No puedes editar etiquetas de otros");
        }

        // 4. Verificar que el nombre nuevo no esté pillado por el mismo usuario
        tagRepository.findByNameAndAuthor(tagDetails.getName(), tag.getAuthor())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BadRequestException("Ya tienes otra etiqueta con ese nombre");
                }
            });

        // 5. Aplicar cambios y guardar
        tag.setName(tagDetails.getName());
        return tagRepository.save(tag);
    }
    //4. BORRAR TAG
    @Transactional
    public void deleteTag(Long id, String username) throws ForbiddenException {
        // 1. Recuperar datos
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));

        // 2. Toda la lógica de seguridad y permisos reside aquí
        if (!tag.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenException("No tienes permiso para borrar este recurso");
        }

        // 3. Ejecución
        tagRepository.delete(tag);
    }
    
    //5. Buscar las tareas de un tag
    public List<Task> getTasksByTagName(String tagName, String username) {
        User user = getUserByUsername(username);
        Tag tag = tagRepository.findByNameAndAuthor(tagName, user)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes la etiqueta: " + tagName));

        return taskRepository.findByTagsContainingAndAuthor(tag, user);
    }
 // Método auxiliar
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
    
    

