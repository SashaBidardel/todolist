package com.example.sashabf.service;

import com.example.sashabf.DTO.PriorityGroupDTO;
import com.example.sashabf.DTO.TaskDashboardDTO;
import com.example.sashabf.exception.BadRequestException;
import com.example.sashabf.exception.ForbiddenException;
import com.example.sashabf.exception.ResourceNotFoundException;
import com.example.sashabf.model.Category;
import com.example.sashabf.model.Priority;
import com.example.sashabf.model.Tag;
import com.example.sashabf.model.Task;
import com.example.sashabf.model.User;
import com.example.sashabf.model.UserRole;
import com.example.sashabf.repository.CategoryRepository;
import com.example.sashabf.repository.TagRepository;
import com.example.sashabf.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    //1. Crear tarea
    public Task createTask(Task task, User author) {
        task.setAuthor(author);
        
        // Si la tarea no trae categoría, buscamos la "General"
        if (task.getCategory() == null) {
            Category general = categoryRepository.findByTitle("General")
                .orElseThrow(() -> new ResourceNotFoundException("Error crítico: La categoría por defecto 'General' no existe."));
            task.setCategory(general);
        }
        
        return taskRepository.save(task);
    }
    //2. Editar tarea
    public Task updateTask(Long id, Task taskDetails, User author) {
        // 1. Buscar la tarea original
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La tarea con ID " + id + " no existe."));

        // 2. Seguridad: Solo el dueño edita
        if ( !task.getAuthor().getId().equals(author.getId())) {
            throw new RuntimeException("No tienes permiso para editar esta tarea.");
        }

        // 3. Actualizamos los campos de texto y estado
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        task.setDeadline(taskDetails.getDeadline());
        task.setPriority(taskDetails.getPriority());

        // 4. Lógica de Tags con List
        if (taskDetails.getTags() != null) {
            List<Tag> verifiedTags = new java.util.ArrayList<>();
            for (Tag tag : taskDetails.getTags()) {
                // Buscamos cada tag por ID para asegurar que existe en el catálogo
                Tag existingTag = tagRepository.findById(tag.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("El Tag con ID " + tag.getId() + " no existe."));
                verifiedTags.add(existingTag);
            }
            task.setTags(verifiedTags); // Hibernate limpia la tabla intermedia y la rellena de nuevo
        }

        return taskRepository.save(task);
    }
    
    //3.  Obtener tareas según el rol (ADMIN ve todo, USER solo lo suyo)
    public List<Task> getTasksByUser(User author) {
        List<Task> tasks;
        if (author.getRole() == UserRole.ADMIN) {
            tasks = taskRepository.findAll();
        } else {
            tasks = taskRepository.findByAuthor(author);
        }

        //Lanzar excepción si no hay tareas 
        if (tasks.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron tareas para este usuario.");
        }
        return tasks;
    }
    
    //4. BORRAR
    public void deleteTask(Long id, User currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada."));

        if (!task.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No puedes borrar una tarea que no es tuya.");
        }

        taskRepository.delete(task);
    }
    
    // 5. Añadir Tag a Tarea
    public Task addTagToTask(Long taskId, Long tagId, User currentUser) throws ForbiddenException {
        // 1. Buscar la tarea y el tag
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));

        // 2. VALIDACIÓN: Solo el dueño de la tarea puede modificarla
        if (!task.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("No puedes añadir etiquetas a una tarea que no te pertenece.");
        }

        // 3. Lógica de negocio: Añadir si no lo tiene ya
        if (!task.getTags().contains(tag)) {
            task.getTags().add(tag);
        }

        return taskRepository.save(task);
    }
    
    // 6. Eliminar Tag a Tarea
    public Task removeTagFromTask(Long taskId, Long tagId, User currentUser) throws ForbiddenException {
        // 1. Buscar la tarea y el tag
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));

        // 2. VALIDACIÓN: Solo el dueño de la tarea puede modificarla
        if (!task.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("No puedes quitar etiquetas de una tarea que no te pertenece.");
        }

        // 3. Lógica de negocio: Quitar el tag de la colección
        task.getTags().remove(tag);

        return taskRepository.save(task);
    }
    
    /**
     * Genera la información para el Dashboard agrupando las tareas por prioridad.
     * No requiere usuario, devuelve todas las tareas accesibles.
     */
    public PriorityGroupDTO getTasksByPriorityDashboard() {
        // 1. Obtenemos todas las tareas de la base de datos
        List<Task> allTasks = taskRepository.findAll();

        // 2. Creamos el contenedor DTO
        PriorityGroupDTO dashboard = new PriorityGroupDTO();

        // 3. Mapeamos cada lista filtrando por su enumerado de prioridad
        dashboard.setLow(filterAndMap(allTasks, Priority.LOW));
        dashboard.setMedium(filterAndMap(allTasks, Priority.MEDIUM));
        dashboard.setHigh(filterAndMap(allTasks, Priority.HIGH));

        return dashboard;
    }

    /**
     * Método privado auxiliar para evitar repetir la lógica de filtrado y conversión.
     */
    private List<TaskDashboardDTO> filterAndMap(List<Task> tasks, Priority priority) {
        return tasks.stream()
                .filter(t -> t.getPriority() == priority)
                .map(t -> new TaskDashboardDTO(
                        t.getId(),
                        t.getTitle(),
                        t.getPriority()
                ))
                .collect(Collectors.toList());
    }
}

