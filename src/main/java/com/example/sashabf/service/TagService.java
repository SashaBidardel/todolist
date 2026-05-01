package com.example.sashabf.service;

import com.example.sashabf.exception.BadRequestException;
import com.example.sashabf.exception.ForbiddenException;
import com.example.sashabf.exception.ResourceNotFoundException;
import com.example.sashabf.model.Tag;
import com.example.sashabf.model.Task;
import com.example.sashabf.model.User;
import com.example.sashabf.model.UserRole;
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

   
    // 1. Crear tag
    public Tag createTag(String name) {
        String cleanName = name.trim().toLowerCase();
        
        // Comprobamos si ya existe
        if (tagRepository.findByName(cleanName).isPresent()) {
            throw new BadRequestException("La etiqueta '" + cleanName + "' ya existe y es de uso global.");
        }
        
        Tag tag = new Tag();
        tag.setName(name);      
        tag.setTasks(new ArrayList<>());      
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
    
   //3. BORRAR
    public void deleteTag(Long id, User currentUser) throws ForbiddenException {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado con ID: " + id));

        // Validación autor
        if (!tag.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Acceso denegado: Solo el creador del Tag puede eliminarlo.");
        }

        // Limpieza de la relación Many-to-Many
        for (Task task : tag.getTasks()) {
            task.getTags().remove(tag);
        }
        
        tagRepository.delete(tag);
    }
    //4 EDITAR
    public Tag updateTag(Long id, Tag tagDetails, User currentUser) throws ForbiddenException {
        // 1. Buscamos el tag en la base de datos
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado con ID: " + id));

        // 2. PRIVACIDAD TOTAL: Solo el autor puede editarlo
        // Comparamos el ID del autor del tag con el ID del usuario que hace la petición
        if (!tag.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("No tienes permiso para editar este Tag, ya que no eres su creador.");
        }

        // 3. Actualizamos solo el nombre 
        tag.setName(tagDetails.getName());

        // 4. Guardamos los cambios
        return tagRepository.save(tag);
    }
    
    // 5. Buscar tareas con tags asignados
    public List<Task> getTasksByTagName(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado: " + name));
        
        return tag.getTasks();
    }
    
}
