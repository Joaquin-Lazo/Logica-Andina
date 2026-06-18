package com.telemetry.telemetry_service.config;

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

    public static final String TELEMETRY_EXCHANGE = "telemetry-exchange";
    public static final String TELEMETRY_QUEUE = "telemetry-gps-queue";
    public static final String TELEMETRY_ROUTING_KEY = "telemetry.gps";

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
    public TopicExchange telemetryExchange() {
        return new TopicExchange(TELEMETRY_EXCHANGE);
    }

    @Bean
    public Queue telemetryGpsQueue() {
        return QueueBuilder.durable(TELEMETRY_QUEUE).build();
    }

    @Bean
    public Binding telemetryBinding(Queue telemetryGpsQueue, TopicExchange telemetryExchange) {
        return BindingBuilder.bind(telemetryGpsQueue).to(telemetryExchange).with(TELEMETRY_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
