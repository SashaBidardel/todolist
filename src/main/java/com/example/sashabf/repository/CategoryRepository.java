package com.example.sashabf.repository;

import com.example.sashabf.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Para validar duplicados por nombre
    Optional<Category> findByTitle(String title);
}

