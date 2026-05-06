package com.example.sashabf.repository;

import com.example.sashabf.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    
  
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
}
