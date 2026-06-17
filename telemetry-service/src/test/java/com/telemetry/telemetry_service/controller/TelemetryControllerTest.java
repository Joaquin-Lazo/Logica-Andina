package com.telemetry.telemetry_service.controller;

import com.telemetry.telemetry_service.model.TelemetryLog;
import com.telemetry.telemetry_service.repository.TelemetryLogRepository;
import com.telemetry.telemetry_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TelemetryController.class)
@AutoConfigureMockMvc(addFilters = false)
class TelemetryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TelemetryLogRepository logRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    private TelemetryLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = new TelemetryLog();
        sampleLog.setIdLog(1L);
        sampleLog.setIdRutaRef(1);
        sampleLog.setLatitud(-33.45);
        sampleLog.setLongitud(-70.66);
        sampleLog.setVelocidadKmh(78.5f);
        sampleLog.setTimestampEvento(LocalDateTime.now());
    }

    @Test
    void getAllLogs_shouldReturnList() throws Exception {
        when(logRepository.findAll()).thenReturn(Arrays.asList(sampleLog));
        mockMvc.perform(get("/api/telemetry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idRutaRef").value(1));
    }

    @Test
    void getLogsByRoute_shouldReturnFiltered() throws Exception {
        when(logRepository.findByIdRutaRefOrderByTimestampEventoDesc(1))
                .thenReturn(Arrays.asList(sampleLog));
        mockMvc.perform(get("/api/telemetry/route/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].latitud").value(-33.45));
    }

    @Test
    void getLogsByRoute_empty_shouldReturnEmptyList() throws Exception {
        when(logRepository.findByIdRutaRefOrderByTimestampEventoDesc(999))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/telemetry/route/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}