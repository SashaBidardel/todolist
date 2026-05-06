package com.example.sashabf.controller;

import com.example.sashabf.DTO.PriorityGroupDTO;
import com.example.sashabf.model.Task;
import com.example.sashabf.model.User;
import com.example.sashabf.service.TaskService;
import com.example.sashabf.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tareas", description = "Gestión de tareas personales y visualización de dashboards")
public class TaskController {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;

    // 1. GET: Mis Tareas
    @Operation(
        summary = "Obtener mis tareas",
        description = "Retorna la lista de tareas pertenecientes al usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tareas recuperada con éxito"),
            @ApiResponse(responseCode = "401", description = "No autorizado: Debes iniciar sesión")
        })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Task>> getAll() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(taskService.getTasksByUser(username));
    }

    // 2. POST: Crear Tarea
    @Operation(
        summary = "Crear nueva tarea",
        description = "Crea una tarea y la asigna automáticamente al usuario que realiza la petición."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tarea creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de la tarea inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
        })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return new ResponseEntity<>(taskService.createTask(task, username), HttpStatus.CREATED);
    }

    // 3. DELETE: Borrar (Dueño o ADMIN)
    @Operation(
        summary = "Eliminar tarea",
        description = "Borra una tarea por ID. El servicio verificará si eres el propietario o ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tarea eliminada con éxito"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido: No puedes borrar una tarea que no es tuya"),
            @ApiResponse(responseCode = "404", description = "La tarea con ese ID no existe")
        })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID de la tarea a eliminar", required = true, example = "101")
    @PathVariable Long id, Principal principal) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        taskService.deleteTask(id, username);
        return ResponseEntity.noContent().build();
    }
    
    // 4. PUT: Editar
    @Operation(
        summary = "Actualizar tarea",
        description = "Modifica los detalles de una tarea. Solo el propietario puede editarla."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarea actualizada con éxito"),
            @ApiResponse(responseCode = "403", description = "Prohibido: Solo el dueño puede editar esta tarea"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
        })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
    @Parameter(description = "ID de la tarea a modificar", required = true, example = "101")
    @PathVariable Long id, @RequestBody Task taskDetails, Principal principal) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        taskService.deleteTask(id, username);
        return ResponseEntity.noContent().build();
    }
    // 5. Dashboard de Prioridades
    @Operation(
        summary = "Dashboard de prioridades",
        description = "Devuelve un resumen de tareas agrupadas por niveles de prioridad (Alta, Media, Baja)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estadísticas del dashboard generadas con éxito"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
        })
    @PreAuthorize("hasAnyAuthority('USER', 'GESTOR', 'ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<PriorityGroupDTO> getDashboard() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(taskService.getTasksByPriorityDashboard(username));
    }
    
    @Operation(summary = "Añadir etiqueta a mi tarea")
    @PostMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<Task> addTag(@Parameter(description = "ID de la etiqueta a añadir", required = true, example = "10")@PathVariable Long taskId, @PathVariable Long tagId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(taskService.addTagToTask(taskId, tagId, username));
    }

    @Operation(summary = "Quitar etiqueta de mi tarea")
    @DeleteMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<Task> removeTag(@Parameter(description = "ID de la etiqueta a eliminar", required = true, example = "10") @PathVariable Long taskId, @PathVariable Long tagId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(taskService.removeTagFromTask(taskId, tagId, username));
    }
}