package com.Users.user_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_requests")
@Data
public class ContactRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String email;
    @Column(length = 1000)
    private String mensaje;
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}