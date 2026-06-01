package com.routes.route_service.service;

import com.routes.route_service.config.RabbitConfig;
import com.routes.route_service.dto.RouteEventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RouteEventProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishRouteEvent(RouteEventDTO event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);
        System.out.println("Evento publicado: Ruta#" + event.idRuta() + " -> " + event.nuevoEstado());
    }
}
