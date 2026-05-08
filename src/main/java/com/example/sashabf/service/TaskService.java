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
import com.example.sashabf.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Autowired
    private UserRepository userRepository;
    
 // 1. CREAR TAREA
    public Task createTask(Task task, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validación de duplicados por título para el mismo usuario
        if (taskRepository.existsByTitleAndAuthor(task.getTitle(), currentUser)) {
            throw new BadRequestException("Ya tienes una tarea llamada: " + task.getTitle());
        }

        // Procesar Tags
        if (task.getTags() != null) {
            List<Tag> processedTags = task.getTags().stream().map(t -> {
                return tagRepository.findByName(t.getName())
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(t.getName());
                        newTag.setAuthor(currentUser);
                        return tagRepository.save(newTag);
                    });
            }).collect(Collectors.toList());
            task.setTags(processedTags);
        }

        task.setAuthor(currentUser);
        task.setCreatedAt(LocalDate.now());
        return taskRepository.save(task);
    }

    // 2. EDITAR TAREA
    public Task updateTask(Long id, Task taskDetails, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La tarea no existe."));

        User user = userRepository.findByUsername(username).get();

        // SEGURIDAD: Solo el dueño puede editar 
        if (!task.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para editar esta tarea.");
        }

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        task.setDeadline(taskDetails.getDeadline());
        task.setPriority(taskDetails.getPriority());

        if (taskDetails.getTags() != null) {
            List<Tag> verifiedTags = taskDetails.getTags().stream()
                .map(t -> tagRepository.findByName(t.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado: " + t.getName())))
                .collect(Collectors.toList());
            task.setTags(verifiedTags);
        }

        return taskRepository.save(task);
    }

    // 3. OBTENER TAREAS (Solo las del autor)
    public List<Task> getTasksByUser(String username) {
        User user = userRepository.findByUsername(username).get();
        
        
        List<Task> tasks = taskRepository.findByAuthor(user);

        if (tasks.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron tareas para este usuario.");
        }
        return tasks;
    }

    // 4. BORRAR TAREA
    public void deleteTask(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada."));

        User user = userRepository.findByUsername(username).get();

        if (!task.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("No puedes borrar una tarea que no es tuya.");
        }

        taskRepository.delete(task);
    }

    // 5. DASHBOARD POR PRIORIDAD (Privado)
    public PriorityGroupDTO getTasksByPriorityDashboard(String username) {
        User user = userRepository.findByUsername(username).get();
        List<Task> myTasks = taskRepository.findByAuthor(user);

        PriorityGroupDTO dashboard = new PriorityGroupDTO();
        dashboard.setLow(filterAndMap(myTasks, Priority.LOW));
        dashboard.setMedium(filterAndMap(myTasks, Priority.MEDIUM));
        dashboard.setHigh(filterAndMap(myTasks, Priority.HIGH));

        return dashboard;
    }

    // 6. GESTIÓN DE TAGS INDIVIDUALES
    public Task addTagToTask(Long taskId, Long tagId, String username) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));
        User user = userRepository.findByUsername(username).get();

        if (!task.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso.");
        }

        if (!task.getTags().contains(tag)) {
            task.getTags().add(tag);
        }
        return taskRepository.save(task);
    }

    public Task removeTagFromTask(Long taskId, Long tagId, String username) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));
        User user = userRepository.findByUsername(username).get();

        if (!task.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso.");
        }

        task.getTags().remove(tag);
        return taskRepository.save(task);
    }

    // AUXILIAR DASHBOARD
    private List<TaskDashboardDTO> filterAndMap(List<Task> tasks, Priority priority) {
        return tasks.stream()
                .filter(t -> t.getPriority() == priority)
                .map(t -> new TaskDashboardDTO(t.getId(), t.getTitle(), t.getPriority()))
                .collect(Collectors.toList());
    }
 // Buscar por Título
    public List<Task> searchByTitle(String title, String username) {
        User user = userRepository.findByUsername(username).get();
        return taskRepository.findByAuthorAndTitleContainingIgnoreCase(user, title);
    }

    // Buscar por Estado (Completada o no)
    public List<Task> searchByStatus(boolean completed, String username) {
        User user = userRepository.findByUsername(username).get();
        return taskRepository.findByAuthorAndCompleted(user, completed);
    }

    // Buscar por Prioridad
    public List<Task> searchByPriority(Priority priority, String username) {
        User user = userRepository.findByUsername(username).get();
        return taskRepository.findByAuthorAndPriority(user, priority);
    }

 // Buscar por Importancia
    public List<Task> searchByImportant(boolean important, String username) {
        // 1. Obtenemos el usuario de la sesión
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Buscamos en el repositorio filtrando por autor y el campo boolean 'important'
        return taskRepository.findByAuthorAndImportant(user, important);
    }
 // Obtener el deadline de una tarea específica
    public LocalDate getTaskDeadline(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));

        User user = userRepository.findByUsername(username).get();

        // Validación de seguridad
        if (!task.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para ver esta tarea.");
        }

        return task.getDeadline();
    }

    // Obtener el tiempo estimado de una tarea específica
    public String getTaskEstimatedTime(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));

        User user = userRepository.findByUsername(username).get();

        // Validación de seguridad
        if (!task.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para ver esta tarea.");
        }

        return task.getEstimatedTime();
    }
    //obtener tareas completadas
    public List<Task> getTasksByStatus(boolean completed, String username) {
        // 1. Buscamos el objeto User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Llamamos al repositorio filtrando por autor y estado
        return taskRepository.findByAuthorAndCompleted(user, completed);
    }

    // Prioridad
    public Priority getTaskPriority(Long id, String username) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        validateOwnership(task, username);
        return task.getPriority();
    }

    // Importancia
    public boolean getTaskImportance(Long id, String username) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        validateOwnership(task, username);
        return task.isImportant();
    }

    /**
     * Método privado para reutilizar la lógica de seguridad
     */
    private void validateOwnership(Task task, String username) {
        User user = userRepository.findByUsername(username).get();
        if (!task.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para acceder a esta tarea.");
        }
    }
}