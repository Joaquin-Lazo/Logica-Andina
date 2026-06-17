package com.Users.user_service.controller;

import com.Users.user_service.dto.AuthRequest;
import com.Users.user_service.dto.UserLoginRequest;
import com.Users.user_service.model.Role;
import com.Users.user_service.model.User;
import com.Users.user_service.repository.UserRepository;
import com.Users.user_service.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "spring.security.user.name=admin",
    "spring.security.user.password=password123"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void login_success_shouldReturnToken() throws Exception {
        when(jwtUtil.generateToken("admin")).thenReturn("mock-token");

        AuthRequest request = new AuthRequest("admin", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"));
    }

    @Test
    void login_failure_shouldReturn401() throws Exception {
        AuthRequest request = new AuthRequest("admin", "wrong");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userLogin_success_shouldReturnToken() throws Exception {
        Role role = new Role();
        role.setNombreRol("ROLE_USER");
        
        User user = new User();
        user.setIdUsuario(1);
        user.setNombres("Test");
        user.setApellidos("User");
        user.setCorreo("test@test.com");
        user.setPasswordHash("hashedpass");
        user.setRol(role);

        when(userRepository.findByCorreo("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedpass")).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com")).thenReturn("mock-user-token");

        UserLoginRequest request = new UserLoginRequest("test@test.com", "password");

        mockMvc.perform(post("/api/auth/user-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-user-token"));
    }

    @Test
    void userLogin_wrongPassword_shouldReturn401() throws Exception {
        User user = new User();
        user.setCorreo("test@test.com");
        user.setPasswordHash("hashedpass");

        when(userRepository.findByCorreo("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashedpass")).thenReturn(false);

        UserLoginRequest request = new UserLoginRequest("test@test.com", "wrong");

        mockMvc.perform(post("/api/auth/user-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void userLogin_userNotFound_shouldReturn401() throws Exception {
        when(userRepository.findByCorreo("notfound@test.com")).thenReturn(Optional.empty());

        UserLoginRequest request = new UserLoginRequest("notfound@test.com", "pass");

        mockMvc.perform(post("/api/auth/user-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
