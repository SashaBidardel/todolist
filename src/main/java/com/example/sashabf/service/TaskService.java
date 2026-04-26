package com.example.sashabf.service;

import com.example.sashabf.model.Category;
import com.example.sashabf.model.Task;
import com.example.sashabf.model.User;
import com.example.sashabf.model.UserRole;
import com.example.sashabf.repository.CategoryRepository;
import com.example.sashabf.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {

    @Autowired
	private TaskRepository taskRepository;
    @Autowired
    private CategoryRepository categoryRepository;
   
 

    public Task createTask(Task task, User author) {
        task.setAuthor(author);
        
        // Si la tarea no trae categoría, buscamos la "General"
        if (task.getCategory() == null) {
            Category general = categoryRepository.findByTitle("General")
                .orElseThrow(() -> new RuntimeException("Error: La categoría 'General' no ha sido inicializada."));
            task.setCategory(general);
        }
        
        return taskRepository.save(task);
    }

    // Obtener tareas según el rol (ADMIN ve todo, USER solo lo suyo)
    public List<Task> getTasksByUser(User author) {
        if (author.getRole() == UserRole.ADMIN) {
            return taskRepository.findAll();
        }
        return taskRepository.findByAuthor(author);
    }

    // Borrado con control de acceso
    public void deleteTask(Long id, User author) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        // Lógica de seguridad: Solo el autor o un ADMIN pueden borrar
        if (author.getRole() == UserRole.ADMIN || task.getAuthor().getId().equals(author.getId())) {
            taskRepository.delete(task);
        } else {
            throw new RuntimeException("No tienes permiso para borrar esta tarea");
        }
    }
}
