package com.Users.user_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.Users.user_service.model.ContactRequest;
import com.Users.user_service.repository.ContactRequestRepository;
import com.Users.user_service.model.User;
import com.Users.user_service.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Endpoints para la gestión de usuarios y contactos")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private ContactRequestRepository contactRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Operation(summary = "Listar todos los usuarios", description = "Retorna una lista con todos los usuarios registrados en el sistema.")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Obtener usuario por ID", description = "Retorna los detalles de un usuario específico.")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Crear nuevo usuario", description = "Crea un nuevo usuario y lo guarda en la base de datos con contraseña hasheada.")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User newUser) {
        try {
            newUser.setPasswordHash(passwordEncoder.encode(newUser.getPasswordHash()));
            User savedUser = userRepository.save(newUser);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Operation(summary = "Obtener peticiones de contacto", description = "Retorna una lista con todos los mensajes de contacto dejados en la web cliente.")
    @GetMapping("/contacts")
    public ResponseEntity<List<ContactRequest>> getAllContacts() {
        return new ResponseEntity<>(contactRequestRepository.findAll(), HttpStatus.OK);
    }

    @Operation(summary = "Crear petición de contacto", description = "Guarda un mensaje de contacto enviado desde la web cliente.")
    @PostMapping("/contacts")
    public ResponseEntity<?> createContact(@RequestBody ContactRequest request) {
        contactRequestRepository.save(request);
        // Simulate sending email for the presentation
        System.out.println("=================================================");
        System.out.println("SIMULACION DE ENVIO DE CORREO (CONTACTO)");
        System.out.println("Para: " + request.getEmail());
        System.out.println("De: " + request.getNombre());
        System.out.println("Mensaje: " + request.getMensaje());
        System.out.println("ESTADO: ENVIADO EXITOSAMENTE A LA BANDEJA DE ENTRADA");
        System.out.println("=================================================");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza la información de un usuario existente. Si se envía una nueva contraseña, esta será hasheada.")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User updatedUserData) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User userToUpdate = existingUser.get();
            
            userToUpdate.setRol(updatedUserData.getRol());
            userToUpdate.setRut(updatedUserData.getRut());
            userToUpdate.setNombres(updatedUserData.getNombres());
            userToUpdate.setApellidos(updatedUserData.getApellidos());
            userToUpdate.setCorreo(updatedUserData.getCorreo());
            userToUpdate.setTelefono(updatedUserData.getTelefono());
            userToUpdate.setEstadoActivo(updatedUserData.getEstadoActivo());
            if (updatedUserData.getPasswordHash() != null &&
            !updatedUserData.getPasswordHash().isEmpty()){
                userToUpdate.setPasswordHash(passwordEncoder.encode(updatedUserData.getPasswordHash()));
            }
            return new ResponseEntity<>(userRepository.save(userToUpdate), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Integer id) {
        try {
            userRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}