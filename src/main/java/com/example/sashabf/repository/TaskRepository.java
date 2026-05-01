package com.example.sashabf.repository;



import com.example.sashabf.model.Category;
import com.example.sashabf.model.Task;
import com.example.sashabf.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // Para que un USER solo vea sus tareas
    List<Task> findByAuthor(User author);
    
    // Filtro por estado de completado
    List<Task> findByAuthorAndCompleted(User author, boolean completed);
    // Bucamos las tareas que tienen una determina categoría
    List<Task> findByCategoryId(Long id);
    
}
