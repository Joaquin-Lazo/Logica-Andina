package com.routes.route_service.controller;

import com.routes.route_service.dto.AuthRequest;
import com.routes.route_service.dto.AuthResponse;
import com.routes.route_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${spring.security.user.name}")
    private String staticUser;

    @Value("${spring.security.user.password}")
    private String staticPass;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        if (staticUser.equals(request.username()) && staticPass.equals(request.password())) {
            String token = jwtUtil.generateToken(staticUser);
            return ResponseEntity.ok(new AuthResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
    }
}