package com.telemetry.telemetry_service.controller;

import com.telemetry.telemetry_service.model.Alert;
import com.telemetry.telemetry_service.model.TelemetryLog;
import com.telemetry.telemetry_service.repository.AlertRepository;
import com.telemetry.telemetry_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertRepository alertRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Alert sampleAlert;

    @BeforeEach
    void setUp() {
        TelemetryLog logGps = new TelemetryLog();
        logGps.setIdLog(1L);
        logGps.setIdRutaRef(1);
        logGps.setLatitud(-33.45);
        logGps.setLongitud(-70.66);
        logGps.setVelocidadKmh(120.0f);

        sampleAlert = new Alert();
        sampleAlert.setIdAlerta(1L);
        sampleAlert.setLogGps(logGps);
        sampleAlert.setTipoAlerta("Exceso de Velocidad");
        sampleAlert.setNivelSeveridad("ALTA");
        sampleAlert.setResuelta(false);
    }

    @Test
    void getAllAlerts_shouldReturnList() throws Exception {
        when(alertRepository.findAll()).thenReturn(Arrays.asList(sampleAlert));
        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoAlerta").value("Exceso de Velocidad"));
    }

    @Test
    void getActiveAlerts_shouldReturnUnresolved() throws Exception {
        when(alertRepository.findByResueltaFalse()).thenReturn(Arrays.asList(sampleAlert));
        mockMvc.perform(get("/api/alerts/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resuelta").value(false));
    }

    @Test
    void getActiveAlerts_empty_shouldReturnEmptyList() throws Exception {
        when(alertRepository.findByResueltaFalse()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/alerts/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}