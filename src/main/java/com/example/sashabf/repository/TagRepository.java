package com.example.sashabf.repository;

import com.example.sashabf.model.Tag;
import com.example.sashabf.model.Task;
import com.example.sashabf.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    
  
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
   //  Para listar solo las etiquetas del usuario logueado
    List<Tag> findByAuthor(User author);

    // Para buscar una etiqueta específica del usuario (por ejemplo, al crear o editar)
    Optional<Tag> findByNameAndAuthor(String name, User author);
   
}
