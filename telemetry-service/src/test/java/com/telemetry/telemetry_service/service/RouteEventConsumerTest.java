package com.telemetry.telemetry_service.service;

import com.telemetry.telemetry_service.dto.RouteEventDTO;
import com.telemetry.telemetry_service.dto.TelemetryLogDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteEventConsumerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RouteEventConsumer routeEventConsumer;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testHandleRouteEvent_EnTransito() {
        RouteEventDTO event = new RouteEventDTO(
                1,
                "En Transito",
                2,
                -33.0,
                -70.0,
                150.0,
                "Origen",
                "Destino"
        );

        routeEventConsumer.handleRouteEvent(event);

        // Verify convertAndSend was called initially
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(TelemetryLogDTO.class));
    }

    @Test
    void testHandleRouteEvent_Completada() {
        RouteEventDTO event = new RouteEventDTO(
                2,
                "Completada",
                3,
                -33.0,
                -70.0,
                150.0,
                "Origen",
                "Destino"
        );

        routeEventConsumer.handleRouteEvent(event);

        // Verify convertAndSend was called at completion
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(TelemetryLogDTO.class));
    }

    @Test
    void testHandleRouteEvent_OtherStatus() {
        RouteEventDTO event = new RouteEventDTO(
                3,
                "Pendiente",
                3,
                -33.0,
                -70.0,
                150.0,
                "Origen",
                "Destino"
        );

        routeEventConsumer.handleRouteEvent(event);

        // Verify convertAndSend was NOT called
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(TelemetryLogDTO.class));
    }
}
