package com.telemetry.telemetry_service.service;

import com.telemetry.telemetry_service.dto.TelemetryLogDTO;
import com.telemetry.telemetry_service.model.Alert;
import com.telemetry.telemetry_service.model.TelemetryLog;
import com.telemetry.telemetry_service.repository.AlertRepository;
import com.telemetry.telemetry_service.repository.TelemetryLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryLogConsumerTest {

    @Mock
    private TelemetryLogRepository logRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private TelemetryLogConsumer telemetryLogConsumer;

    @Test
    void testConsumeTelemetryLog() {
        TelemetryLogDTO dto = new TelemetryLogDTO(
                1,
                -33.0,
                -70.0,
                85.5f,
                LocalDateTime.now()
        );

        when(logRepository.save(any(TelemetryLog.class))).thenReturn(new TelemetryLog());

        telemetryLogConsumer.consumeTelemetryLog(dto);

        verify(logRepository, times(1)).save(any(TelemetryLog.class));
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void testConsumeTelemetryLog_WithAlert() {
        TelemetryLogDTO dto = new TelemetryLogDTO(
                1,
                -33.0,
                -70.0,
                95.0f,
                LocalDateTime.now()
        );

        when(logRepository.save(any(TelemetryLog.class))).thenReturn(new TelemetryLog());
        when(alertRepository.save(any(Alert.class))).thenReturn(new Alert());

        telemetryLogConsumer.consumeTelemetryLog(dto);

        verify(logRepository, times(1)).save(any(TelemetryLog.class));
        verify(alertRepository, times(1)).save(any(Alert.class));
    }
}
