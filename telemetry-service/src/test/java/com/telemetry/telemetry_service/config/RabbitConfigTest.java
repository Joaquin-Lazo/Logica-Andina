package com.telemetry.telemetry_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class RabbitConfigTest {

    private final RabbitConfig rabbitConfig = new RabbitConfig();

    @Test
    void testRouteExchange() {
        TopicExchange exchange = rabbitConfig.routeExchange();
        assertNotNull(exchange);
        assertEquals(RabbitConfig.EXCHANGE, exchange.getName());
    }

    @Test
    void testRouteEventsQueue() {
        Queue queue = rabbitConfig.routeEventsQueue();
        assertNotNull(queue);
        assertEquals(RabbitConfig.QUEUE, queue.getName());
    }

    @Test
    void testRouteBinding() {
        Queue queue = new Queue(RabbitConfig.QUEUE);
        TopicExchange exchange = new TopicExchange(RabbitConfig.EXCHANGE);

        Binding binding = rabbitConfig.routeBinding(queue, exchange);
        
        assertNotNull(binding);
        assertEquals(RabbitConfig.QUEUE, binding.getDestination());
        assertEquals(RabbitConfig.EXCHANGE, binding.getExchange());
        assertEquals(RabbitConfig.ROUTING_KEY, binding.getRoutingKey());
    }

    @Test
    void testJMessageConverter() {
        MessageConverter converter = rabbitConfig.jMessageConverter();
        assertNotNull(converter);
    }
}
