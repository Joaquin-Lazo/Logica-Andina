package com.Users.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "User Service API - Logística Andina",
        version = "1.0",
        description = "Microservicio encargado de la gestión de usuarios, roles, autenticación JWT y solicitudes de contacto.",
        contact = @Contact(name = "Equipo de Desarrollo", email = "soporte@logisticaandina.cl")
    )
)
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
