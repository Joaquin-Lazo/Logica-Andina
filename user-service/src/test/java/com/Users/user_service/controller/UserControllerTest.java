package com.Users.user_service.controller;

import com.Users.user_service.model.ContactRequest;
import com.Users.user_service.model.Role;
import com.Users.user_service.model.User;
import com.Users.user_service.repository.ContactRequestRepository;
import com.Users.user_service.repository.UserRepository;
import com.Users.user_service.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ContactRequestRepository contactRequestRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtUtil jwtUtil;

    private User sampleUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = new Role();
        sampleRole.setIdRol(1);
        sampleRole.setNombreRol("ROLE_ADMINISTRADOR");
        sampleRole.setDescripcion("Administrador del sistema");
        sampleRole.setActivo(true);

        sampleUser = new User();
        sampleUser.setIdUsuario(1);
        sampleUser.setRol(sampleRole);
        sampleUser.setRut("11111111-1");
        sampleUser.setNombres("Admin");
        sampleUser.setApellidos("Sistema");
        sampleUser.setCorreo("admin@transandina.cl");
        sampleUser.setTelefono("+56912345678");
        sampleUser.setPasswordHash("hashed_password");
        sampleUser.setEstadoActivo(true);
    }

    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        when(userRepository.findAll()).thenReturn(Arrays.asList(sampleUser));
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombres").value("Admin"));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("admin@transandina.cl"));
    }

    @Test
    void getUserById_notFound_shouldReturn404() throws Exception {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createContact_shouldReturnOk() throws Exception {
        ContactRequest cr = new ContactRequest();
        cr.setId(1L);
        cr.setNombre("Test User");
        cr.setEmail("test@mail.com");
        cr.setMensaje("Necesito una cotización");
        when(contactRequestRepository.save(any(ContactRequest.class))).thenReturn(cr);

        // NOTE: The actual URL is /api/users/api/contacts due to the double-path bug.
        // If you fix the bug (change @PostMapping to "/contacts"), change this URL to /api/users/contacts
        mockMvc.perform(post("/api/users/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cr)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        doNothing().when(userRepository).deleteById(1);
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createUser_shouldReturnCreated() throws Exception {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        mockMvc.perform(post("/api/users")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUser)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateUser_shouldReturnUpdated() throws Exception {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode(anyString())).thenReturn("new_hash");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        
        sampleUser.setPasswordHash("new_password");
        
        mockMvc.perform(put("/api/users/1")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUser)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_notFound_shouldReturn404() throws Exception {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/users/99")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUser)))
                .andExpect(status().isNotFound());
    }
}