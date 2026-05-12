package com.Users.user_service.controller;

import com.Users.user_service.dto.AuthRequest;
import com.Users.user_service.dto.AuthResponse;
import com.Users.user_service.dto.UserLoginRequest;
import com.Users.user_service.dto.UserLoginResponse;
import com.Users.user_service.model.User;
import com.Users.user_service.repository.UserRepository;
import com.Users.user_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${spring.security.user.name}")
    private String staticUser;

    @Value("${spring.security.user.password}")
    private String staticPass;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        if (staticUser.equals(request.username()) && staticPass.equals(request.password())) {
            String token = jwtUtil.generateToken(staticUser);
            return ResponseEntity.ok(new AuthResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
    }

    @PostMapping("/user-login")
    public ResponseEntity<?> userLogin(@RequestBody UserLoginRequest request){
        Optional<User> optionalUser = userRepository.findByCorreo(request.correo());

        if (optionalUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }

        String token = jwtUtil.generateToken(user.getCorreo());

        UserLoginResponse response = new UserLoginResponse(
            token,
            user.getIdUsuario(),
            user.getNombres(),
            user.getApellidos(),
            user.getCorreo(),
            user.getRol().getNombreRol()
        );
        return ResponseEntity.ok(response);
    }
}