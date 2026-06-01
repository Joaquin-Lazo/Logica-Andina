package com.telemetry.telemetry_service.service;

import com.telemetry.telemetry_service.config.RabbitConfig;
import com.telemetry.telemetry_service.dto.RouteEventDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RouteEventConsumer {
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleRouteEvent(RouteEventDTO event) {
        System.out.println("====== EVENTO RABBITMQ RECIBIDO ======");
        System.out.println("Ruta ID: " + event.idRuta());
        System.out.println("Nuevo Estado: " + event.nuevoEstado());
        System.out.println("======================================");

        // Logica por añadir
    }
}