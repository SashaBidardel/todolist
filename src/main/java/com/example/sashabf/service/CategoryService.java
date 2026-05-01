package com.example.sashabf.service;

import com.example.sashabf.exception.BadRequestException;
import com.example.sashabf.exception.ResourceNotFoundException;
import com.example.sashabf.model.Category;
import com.example.sashabf.model.Task;
import com.example.sashabf.repository.CategoryRepository;
import com.example.sashabf.repository.TaskRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
	private CategoryRepository categoryRepository;
    @Autowired
    private TaskRepository taskRepository;
   

    //1. Listar todas (lo usarán tanto USER como ADMIN)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    //2. Crear categoría con validación de duplicados
    public Category createCategory(Category category) {
        if (categoryRepository.findByTitle(category.getTitle()).isPresent()) {
            throw new BadRequestException("La categoría '" + category.getTitle() + "' ya existe.");
        }
        return categoryRepository.save(category);
    }

    //3. Obtener una por ID (útil para asignar a una tarea)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    //4. Borrar categoría (Solo ADMIN)
    @Transactional
    public void deleteCategory(Long idABorrar) {
        Category general = categoryRepository.findByTitle("General").get();
        
        // Si intentan borrar la categoría general, lo impedimos
        if (idABorrar.equals(general.getId())) {
            throw new BadRequestException("No se puede borrar la categoría por defecto");
        }

        // Buscamos todas las tareas que pertenecen a la categoría que vamos a borrar
        List<Task> tasks = taskRepository.findByCategoryId(idABorrar);
        
        // Las movemos a General
        for (Task task : tasks) {
            task.setCategory(general);
        }
        taskRepository.saveAll(tasks); // Actualizamos las tareas

        // Ahora ya podemos borrar la categoría de forma segura
        categoryRepository.deleteById(idABorrar);
    }
    // 5. Actualizar Categoría
    public Category updateCategory(Long id, Category categoryDetails) {
        // 1. Buscar la categoría
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        // 2. Validación: No permitir renombrar a "General" o nombres reservados 
        if (category.getTitle().equalsIgnoreCase("General")) {
            throw new BadRequestException("La categoría 'General' es del sistema y no se puede editar.");
        }

        // 3. Actualizar campos
        category.setTitle(categoryDetails.getTitle());
        
        
        return categoryRepository.save(category);
    }
}
