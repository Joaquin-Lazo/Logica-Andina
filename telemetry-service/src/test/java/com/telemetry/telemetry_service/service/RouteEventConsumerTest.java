package com.telemetry.telemetry_service.service;

import com.telemetry.telemetry_service.dto.RouteEventDTO;
import com.telemetry.telemetry_service.model.TelemetryLog;
import com.telemetry.telemetry_service.repository.TelemetryLogRepository;
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
    private TelemetryLogRepository logRepository;

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

        when(logRepository.save(any(TelemetryLog.class))).thenReturn(new TelemetryLog());

        routeEventConsumer.handleRouteEvent(event);

        // Verify saveLog was called initially
        verify(logRepository, times(1)).save(any(TelemetryLog.class));
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

        when(logRepository.save(any(TelemetryLog.class))).thenReturn(new TelemetryLog());

        routeEventConsumer.handleRouteEvent(event);

        // Verify saveLog was called at completion
        verify(logRepository, times(1)).save(any(TelemetryLog.class));
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

        // Verify saveLog was NOT called
        verify(logRepository, never()).save(any(TelemetryLog.class));
    }
}
