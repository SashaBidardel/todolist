package com.example.sashabf.service;

import com.example.sashabf.model.User;
import com.example.sashabf.model.UserRole;
import com.example.sashabf.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  BCryptPasswordEncoder passwordEncoder;

   

    // Registro de nuevo usuario
    public User registerUser(User user) {
        // 1. Validar si el nombre de usuario ya existe
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }

        // 2. CIFRAR la contraseña antes de guardar
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

     // No nos importa lo que traiga el objeto 'user', aquí mandamos nosotros
        user.setRole(UserRole.USER);

        return userRepository.save(user);
    }

    // Buscar por nombre de usuario (útil para el login)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Listar todos (Solo lo usará el ADMIN)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Borrar usuario
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("El usuario con ID " + id + " no existe.");
        }
        userRepository.deleteById(id);
    }
}
