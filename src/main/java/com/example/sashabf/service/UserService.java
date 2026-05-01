package com.example.sashabf.service;

import com.example.sashabf.exception.BadRequestException;
import com.example.sashabf.exception.ResourceNotFoundException;
import com.example.sashabf.model.User;
import com.example.sashabf.model.UserRole;
import com.example.sashabf.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.BeanDefinitionDsl.Role;
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

   

    //1. Registro de nuevo usuario
    public User registerUser(User user) {
        // 1. Validar si el nombre de usuario ya existe
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new BadRequestException("El nombre de usuario ya está en uso.");
        }
        
        // 2. Validar si el email ya existe (IGUAL QUE EL ANTERIOR)
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BadRequestException("El correo electrónico ya está registrado.");
        }
        
        // 2. CIFRAR la contraseña antes de guardar
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // No nos importa lo que traiga el objeto 'user', aquí mandamos nosotros
        user.setRole(UserRole.USER);

        return userRepository.save(user);
    }
    
    //2. Editar usuario
    public User updateUser(Long id, User userDetails, User currentUser) {
        // 1. Buscar el usuario existente
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        // 2. Seguridad: Un usuario solo puede editarse a sí mismo, a menos que sea ADMIN
        if (currentUser.getRole() != UserRole.ADMIN && !user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("No tienes permiso para editar este perfil.");
        }

        // 3. Actualizar campos básicos
        user.setFullname(userDetails.getFullname());
        user.setEmail(userDetails.getEmail());
        user.setUsername(userDetails.getUsername());

        // 4. Lógica de Contraseña: Solo se actualiza si viene una nueva
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            // Ciframos la nueva contraseña antes de guardar
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        // 5. Lógica de Rol: Solo un ADMIN puede cambiar roles
        if (currentUser.getRole() == UserRole.ADMIN && userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        return userRepository.save(user);
    }

    //3.  Buscar por nombre de usuario (útil para el login)
 
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario con username '" + username + "' no encontrado."));
    }

  //4. Listar todos (Solo lo usará el ADMIN)
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No hay usuarios registrados en el sistema.");
        }
        return users;
    }

    //5.  Borrar usuario
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("El usuario con ID " + id + " no existe.");
        }
        userRepository.deleteById(id);
    }
    
    //6. Cambiar contraseña
    public void changePassword(String newPassword, User currentUser) {
        // 1. Ciframos la nueva contraseña antes de cualquier otra cosa
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        // 2. Actualizamos el objeto del usuario actual
        currentUser.setPassword(encodedPassword);
        
        // 3. Guardamos en la base de datos
        userRepository.save(currentUser);
    }
    
    //7. Promocionar a Gestor
    public void promoteToGestor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        user.setRole(UserRole.GESTOR); // Cambiamos el rol a GESTOR
        userRepository.save(user);
    }
    
   //8 Degradar a User
    public void demoteToUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        user.setRole(UserRole.USER); // Cambiamos el rol a USER
        userRepository.save(user);
    }
}
