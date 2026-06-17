package com.telemetry.telemetry_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "Telemetry Service API - Logística Andina",
        version = "1.0",
        description = "Microservicio encargado de la ingesta de datos GPS, análisis en tiempo real y generación de alertas mediante RabbitMQ y MongoDB.",
        contact = @Contact(name = "Equipo de Desarrollo", email = "soporte@logisticaandina.cl")
    )
)
public class TelemetryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TelemetryServiceApplication.class, args);
    }
}
