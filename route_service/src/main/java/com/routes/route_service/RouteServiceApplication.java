package com.routes.route_service;

import org.springframework.boot.SpringApplication; 
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Route Service API - Logística Andina",
        version = "1.0",
        description = "Microservicio para la gestión de rutas, camiones, cargamentos, clientes y facturación de la empresa de transportes Logística Andina.",
        contact = @Contact(name = "Equipo de Desarrollo", email = "soporte@logisticaandina.cl")
    )
)
public class RouteServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RouteServiceApplication.class, args);
	}

}
