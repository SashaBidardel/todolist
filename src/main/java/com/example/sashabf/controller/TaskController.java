package com.example.sashabf.controller;

import com.example.sashabf.model.Task;
import com.example.sashabf.model.User;
import com.example.sashabf.service.TaskService;
import com.example.sashabf.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private  UserService userService;

    

    // GET: /api/tasks -> Obtiene tareas según el rol del que pregunta
    @GetMapping
    public ResponseEntity<List<Task>> getMyTasks(Principal principal) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado"));
        
        return ResponseEntity.ok(taskService.getTasksByUser(user));
    }

    // POST: /api/tasks -> Crea una tarea para el usuario actual
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task, Principal principal) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado"));
        
        return new ResponseEntity<>(taskService.createTask(task, user), HttpStatus.CREATED);
    }

    // DELETE: /api/tasks/{id} -> Borra si eres dueño o ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado"));
        
        taskService.deleteTask(id, user);
        return ResponseEntity.noContent().build();
    }
}
