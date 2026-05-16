package com.example.sashabf.service;

import com.example.sashabf.DTO.UpdateProfileDTO;
import com.example.sashabf.exception.BadRequestException;
import com.example.sashabf.exception.ResourceNotFoundException;
import com.example.sashabf.model.User;
import com.example.sashabf.model.UserRole;
import com.example.sashabf.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.BeanDefinitionDsl.Role;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public User updateUser(Long id, User userDetails) {
        // 1. Obtener el username de la sesión actual
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        
        System.out.println("--- DEBUG INICIO ---");
        System.out.println("Usuario autenticado: [" + currentUsername + "]");

        // 2. Buscar el usuario que está operando
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new BadRequestException("No se encontró el usuario de la sesión: " + currentUsername));

        // 3. Buscar el usuario que se quiere editar (el de la URL)
        User userToEdit = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con ID: " + id));

        // 4. LOGS DE COMPARACIÓN
        System.out.println("ID en Sesión: " + currentUser.getId() + " | ID a Editar: " + userToEdit.getId());
        System.out.println("Rol del que opera: " + currentUser.getRole());

        // 5. LÓGICA DE SEGURIDAD
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        boolean isOwner = currentUser.getId().longValue() == userToEdit.getId().longValue();

        if (!isAdmin && !isOwner) {
            System.out.println("!!! BLOQUEO: Ni Admin ni Dueño");
            throw new BadRequestException("No tienes permiso para editar este perfil.");
        }

     // 6. ACTUALIZAR CAMPOS (De forma segura, solo si vienen en el JSON)
        if (userDetails.getFullname() != null && !userDetails.getFullname().trim().isEmpty()) {
            userToEdit.setFullname(userDetails.getFullname());
        }
        
        if (userDetails.getEmail() != null && !userDetails.getEmail().trim().isEmpty() && !userDetails.getEmail().equals("string")) {
            userToEdit.setEmail(userDetails.getEmail());
        }
        
        if (userDetails.getUsername() != null && !userDetails.getUsername().trim().isEmpty() && !userDetails.getUsername().equals("string")) {
            userToEdit.setUsername(userDetails.getUsername());
        }

        // 7. CONTRASEÑA (Solo si se envía una nueva y no es el "string" por defecto)
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty() && !userDetails.getPassword().equals("string")) {
            userToEdit.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        // 8. ROL (Solo el ADMIN puede cambiar roles)
        if (isAdmin && userDetails.getRole() != null) {
            userToEdit.setRole(userDetails.getRole());
        }

        System.out.println("--- UPDATE EXITOSO ---");
        return userRepository.save(userToEdit);
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
 
 // Método exclusivo para que el usuario se edite a sí mismo desde su perfil
    public User updateUserByUsername(String usernameActual, UpdateProfileDTO profileDto) {
        // 1. Buscamos el usuario de la sesión (el dueño del perfil)
        User userToEdit = userRepository.findByUsername(usernameActual)
                .orElseThrow(() -> new BadRequestException("No se encontró el usuario de la sesión: " + usernameActual));

        System.out.println("--- ACTUALIZANDO DESDE PERFIL ---");
        System.out.println("Usuario: " + userToEdit.getUsername());

        // 2. Actualizamos los campos de forma segura (igual que con el admin)
        if (profileDto.getFullname() != null && !profileDto.getFullname().trim().isEmpty()) {
            userToEdit.setFullname(profileDto.getFullname());
        }
        
        if (profileDto.getEmail() != null && !profileDto.getEmail().trim().isEmpty() && !profileDto.getEmail().equals("string")) {
            userToEdit.setEmail(profileDto.getEmail());
        }
        
        if (profileDto.getUsername() != null && !profileDto.getUsername().trim().isEmpty() && !profileDto.getUsername().equals("string")) {
            userToEdit.setUsername(profileDto.getUsername());
        }

        // 3. Contraseña
        if (profileDto.getPassword() != null && !profileDto.getPassword().isEmpty() && !profileDto.getPassword().equals("string")) {
            userToEdit.setPassword(passwordEncoder.encode(profileDto.getPassword()));
        }

        // NOTA: Aquí no tocamos el Rol. El usuario no puede cambiarse su propio rol.

        // 4. Guardamos directamente este objeto
        return userRepository.save(userToEdit);
    }
}
