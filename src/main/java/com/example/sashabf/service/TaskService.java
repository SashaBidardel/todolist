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
        // 1. Obtener el usuario autenticado
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Validación de duplicados: No permitir dos tareas iguales para el mismo usuario
        if (taskRepository.existsByTitleAndAuthor(task.getTitle(), currentUser)) {
            throw new BadRequestException("Ya tienes una tarea llamada: " + task.getTitle());
        }

        // 3. Lógica de Categoría 
        if (task.getCategory() != null && task.getCategory().getTitle() != null) {
            String catTitle = task.getCategory().getTitle();
            // Buscamos la categoría por el nombre que viene en el JSON
            Category existingCategory = categoryRepository.findByTitle(catTitle)
                    .orElseThrow(() -> new ResourceNotFoundException("La categoría '" + catTitle + "' no existe. Créala primero."));
            task.setCategory(existingCategory);
        } else {
            // Si el JSON no trae categoría, asignamos 'General' por defecto
            Category general = categoryRepository.findByTitle("General")
                    .orElseThrow(() -> new ResourceNotFoundException("Error: La categoría 'General' debe existir en la base de datos."));
            task.setCategory(general);
        }

        // 4. Procesar Tags
        if (task.getTags() != null) {
            List<Tag> processedTags = task.getTags().stream().map(t -> 
                tagRepository.findByNameAndAuthor(t.getName(), currentUser)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(t.getName());
                        newTag.setAuthor(currentUser);
                        return tagRepository.save(newTag);
                    })
            ).collect(Collectors.toList());
            task.setTags(processedTags);
        }

        // 5. Configuración de metadatos y persistencia
        task.setAuthor(currentUser);
        task.setCreatedAt(LocalDate.now()); // Fecha de creación automática
        
        return taskRepository.save(task);
    }

    // 2. EDITAR TAREA
    public Task updateTask(Long id, Task taskDetails, String username) {
        // 1. Buscamos la tarea original para no perder el Author ni el ID
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La tarea no existe."));

        // 2. Seguridad: Validar propiedad
        validateOwnership(task, username);

        // 3. Actualizamos todos los atributos
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        task.setDeadline(taskDetails.getDeadline());
        task.setPriority(taskDetails.getPriority());
        task.setEstimatedTime(taskDetails.getEstimatedTime());
        task.setImportant(taskDetails.isImportant());

        // 4. Gestión de Categoría por NOMBRE (Igual que en la creación)
        if (taskDetails.getCategory() != null && taskDetails.getCategory().getTitle() != null) {
            String catTitle = taskDetails.getCategory().getTitle();
            // Buscamos la categoría por el nombre que viene en el JSON
            Category existingCategory = categoryRepository.findByTitle(catTitle)
                    .orElseThrow(() -> new ResourceNotFoundException("La categoría '" + catTitle + "' no existe."));
            task.setCategory(existingCategory);
        } 
        // Si el JSON de edición no envía categoría, mantenemos la que ya tenía la tarea
        // para evitar que pase a "General" por accidente si el usuario solo quería editar el título.

        // 5. Persistencia (Hibernate hará un UPDATE)
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

 // 6. AÑADIR TAG A TAREA
    public Task addTagToTask(Long taskId, Long tagId, String username) {
        // 1. Recuperamos los recursos
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));

        // 2. SEGURIDAD: Validar que el usuario es dueño de AMBOS recursos
        // La tarea debe ser mía
        if (!task.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenException("No tienes permiso sobre esta tarea");
        }
        // El tag también debe ser mío (para evitar usar etiquetas de otros)
        if (!tag.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenException("No puedes usar una etiqueta que no te pertenece");
        }

        // 3. Lógica: Añadir si no existe ya
        if (!task.getTags().contains(tag)) {
            task.getTags().add(tag);
        }
        
        return taskRepository.save(task);
    }

    // 7. QUITAR TAG DE TAREA
    public Task removeTagFromTask(Long taskId, Long tagId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrado"));

        // 2. SEGURIDAD: Solo el dueño de la tarea puede quitar etiquetas
        if (!task.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenException("No tienes permiso para modificar esta tarea");
        }

        // 3. Lógica: Simplemente removemos (si no existe, Java no hace nada)
        task.getTags().remove(tag);
        
        return taskRepository.save(task);
    }

    //8. AUXILIAR DASHBOARD
    private List<TaskDashboardDTO> filterAndMap(List<Task> tasks, Priority priority) {
        return tasks.stream()
                .filter(t -> t.getPriority() == priority)
                .map(t -> new TaskDashboardDTO(t.getId(), t.getTitle(), t.getPriority()))
                .collect(Collectors.toList());
    }
    //9. Buscar por Título
    public List<Task> searchByTitle(String title, String username) {
        User user = userRepository.findByUsername(username).get();
        return taskRepository.findByAuthorAndTitleContainingIgnoreCase(user, title);
    }

    //10. Buscar por Estado (Completada o no)
    public List<Task> searchByStatus(boolean completed, String username) {
        User user = userRepository.findByUsername(username).get();
        return taskRepository.findByAuthorAndCompleted(user, completed);
    }

    //11. Buscar por Prioridad
    public List<Task> searchByPriority(Priority priority, String username) {
        User user = userRepository.findByUsername(username).get();
        return taskRepository.findByAuthorAndPriority(user, priority);
    }

   //12. Buscar por Importancia
    public List<Task> searchByImportant(boolean important, String username) {
        // 1. Obtenemos el usuario de la sesión
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Buscamos en el repositorio filtrando por autor y el campo boolean 'important'
        return taskRepository.findByAuthorAndImportant(user, important);
    }
 //13. Obtener el deadline de una tarea específica
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

    //14. Obtener el tiempo estimado de una tarea específica
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
    //15. obtener tareas completadas
    public List<Task> getTasksByStatus(boolean completed, String username) {
        // 1. Buscamos el objeto User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Llamamos al repositorio filtrando por autor y estado
        return taskRepository.findByAuthorAndCompleted(user, completed);
    }

    //16.  Prioridad
    public Priority getTaskPriority(Long id, String username) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
        validateOwnership(task, username);
        return task.getPriority();
    }

    //17.  Importancia
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