package com.example.sashabf.security;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {
	
	@Bean
	CommandLineRunner initDatabase(
	        UserRepository repository, 
	        CategoryRepository categoryRepository, 
	        TaskRepository taskRepository, 
	        TagRepository tagRepository,   
	        BCryptPasswordEncoder encoder) {
	    
	    return args -> {
	        // 1. Crear el ADMIN
	        if (repository.findByUsername("admin").isEmpty()) {
	            User admin = new User();
	            admin.setUsername("admin");
	            admin.setPassword(encoder.encode("1234")); 
	            admin.setRole(UserRole.ADMIN);
	            admin.setEmail("admin@example.com");
	            admin.setFullname("Administrador");
	            repository.save(admin);
	            System.out.println("Usuario admin creado.");
	        }

	        // 2. Crear el USUARIO1
	        User user1 = repository.findByUsername("usuario1").orElseGet(() -> {
	            User u = new User();
	            u.setUsername("usuario1");
	            u.setPassword(encoder.encode("usuario1")); // Contraseña igual al nombre
	            u.setRole(UserRole.USER);
	            u.setEmail("usuario1@example.com");
	            u.setFullname("Usuario Uno");
	            System.out.println("Usuario 'usuario1' creado.");
	            return repository.save(u);
	        });

	        // 3. Crear la CATEGORÍA GENERAL
	        Category general = categoryRepository.findByTitle("General").orElseGet(() -> {
	            Category c = new Category();
	            c.setTitle("General");
	            System.out.println("Categoría 'General' creada.");
	            return categoryRepository.save(c);
	        });

	        // 4. Crear el TAG creado por usuario1
	        if (tagRepository.findByName("tagusuario1").isEmpty()) {
	            Tag tag = new Tag();
	            tag.setName("tagusuario1");
	            tag.setAuthor(user1); // Asociamos el creador
	            tagRepository.save(tag);
	            System.out.println("Tag 'tagusuario1' creado por usuario1.");
	        }

	     // 5. Crear la TAREA creada por usuario1
	     // 1. Buscamos si la tarea ya existe
	        Optional<Task> existingTask = taskRepository.findByAuthorAndTitleContainingIgnoreCase(user1, "tareausuario1").stream().findFirst();

	        Task task;
	        if (existingTask.isEmpty()) {
	            // Si no existe, la creamos nueva
	            task = new Task();
	            task.setTitle("tareausuario1");
	            task.setAuthor(user1);
	            System.out.println("Creando tarea nueva...");
	        } else {
	            // Si ya existe, la recuperamos para actualizarla
	            task = existingTask.get();
	            System.out.println("Actualizando tarea existente...");
	        }

	        // 2. Seteamos los campos (esto se ejecutará siempre, sea nueva o vieja)
	        task.setDescription("Descripción actualizada");
	        task.setCategory(general);
	        task.setPriority(Priority.LOW);
	        task.setDeadline(LocalDate.now().plusDays(7));
	        task.setEstimatedTime("3 horas");
	        task.setImportant(true);

	        // 3. Guardamos los cambios
	        taskRepository.save(task);
	    };
	}
}
