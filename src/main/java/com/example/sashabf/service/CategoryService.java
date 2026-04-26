package com.example.sashabf.service;

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
   

    // Listar todas (lo usarán tanto USER como ADMIN)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Crear categoría con validación de duplicados
    public Category createCategory(Category category) {
        if (categoryRepository.findByTitle(category.getTitle()).isPresent()) {
            throw new RuntimeException("La categoría '" + category.getTitle() + "' ya existe.");
        }
        return categoryRepository.save(category);
    }

    // Obtener una por ID (útil para asignar a una tarea)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }

    // Borrar categoría (Solo ADMIN)
    @Transactional
    public void deleteCategory(Long idABorrar) {
        Category general = categoryRepository.findByTitle("General").get();
        
        // Si intentan borrar la categoría general, lo impedimos
        if (idABorrar.equals(general.getId())) {
            throw new RuntimeException("No se puede borrar la categoría por defecto");
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
}
