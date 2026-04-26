package com.example.sashabf.security;

import com.example.sashabf.model.Category;
import com.example.sashabf.model.User;
import com.example.sashabf.model.UserRole;
import com.example.sashabf.repository.CategoryRepository;
import com.example.sashabf.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {
	
    @Bean
    CommandLineRunner initDatabase(UserRepository repository,CategoryRepository categoryRepository, BCryptPasswordEncoder encoder) {
        return args -> {
        	//Creamos el admin
            // Comprobamos si ya existe para no duplicarlo cada vez que reiniciemos
            if (repository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                // Ciframos la contraseña 1234
                admin.setPassword(encoder.encode("1234")); 
                admin.setRole(UserRole.ADMIN);
                admin.setEmail("admin@example.com");
                admin.setFullname("admin");
                repository.save(admin);
                
                System.out.println("admin creado: admin/1234");
              
               
            }
            //Creamos la categoría general 
            if (categoryRepository.findByTitle("General").isEmpty()) {
                 Category general = new Category();
                 general.setTitle("General");
                 categoryRepository.save(general);
                 System.out.println("✅ Categoría 'General' creada.");
            }
            if (categoryRepository.count()==0) {
         	   System.out.println("No se creó la categoría general");
            }
        };
       
    }
}
