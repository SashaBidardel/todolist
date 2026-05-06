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

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class TagService {

    @Autowired
	private TagRepository tagRepository;
    @Autowired
	private UserRepository userRepository;
    @Autowired
   	private TaskRepository taskRepository;
   
    // 1. Crear tag
    public Tag createTag(String name, String username) {
        // 1. Buscamos al usuario (para tener su ID)
        User user = userRepository.findByUsername(username).get(); 

        // 2. Creamos el Tag y le asignamos ese usuario
        Tag tag = new Tag();
        tag.setName(name);
        tag.setAuthor(user); // <--- Esto es lo que rellena el user_id en la BD

        // 3. Guardamos
        return tagRepository.save(tag);
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
    //2.  LISTAR TODAS: Recupera todas las etiquetas de la base de datos
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
    
   //3. ACTUALIZAR TAG
    public Tag updateTag(Long id, String newName, String username) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Solo el dueño o un ADMIN pueden editar
        if (user.getRole() != UserRole.ADMIN && !tag.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("No puedes editar tags de otros usuarios");
        }

        tag.setName(newName);
        return tagRepository.save(tag);
    }

    //4. BORRAR TAG
    public void deleteTag(Long id, String username) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Solo el dueño o un ADMIN pueden borrar
        if (user.getRole() != UserRole.ADMIN && !tag.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("No puedes borrar tags de otros usuarios");
        }

        tagRepository.delete(tag);
    }
    
    //5. Buscar las tareas de un tag
    public List<Task> getTasksByTagName(String name) {
        // 1. Validamos si el tag existe (opcional, pero recomendado)
        if (!tagRepository.existsByName(name)) {
            throw new ResourceNotFoundException("Tag no encontrado: " + name);
        }
        
        // 2. Buscamos todas las tareas que contienen ese tag por su nombre
        return taskRepository.findByTags_Name(name);
    }
}
    
    

