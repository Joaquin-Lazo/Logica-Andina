package com.routes.route_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "route-exchange";
    public static final String QUEUE = "route-events-queue";
    public static final String ROUTING_KEY = "route.status";

    @Bean
    public TopicExchange routeExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue routeEventsQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding routeBinding(Queue routeEventsQueue, TopicExchange routeExchange) {
        return BindingBuilder.bind(routeEventsQueue).to(routeExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
