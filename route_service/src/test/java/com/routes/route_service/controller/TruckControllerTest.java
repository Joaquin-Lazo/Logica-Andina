package com.routes.route_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routes.route_service.model.Truck;
import com.routes.route_service.repository.TruckRepository;
import com.routes.route_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TruckController.class)
@AutoConfigureMockMvc(addFilters = false)
class TruckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TruckRepository camionRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Truck sampleTruck;

    @BeforeEach
    void setUp() {
        sampleTruck = new Truck();
        sampleTruck.setIdCamion(1);
        sampleTruck.setPatente("AB-CD-12");
        sampleTruck.setMarcaModelo("Volvo FH16");
        sampleTruck.setCapacidadMaxToneladas(30.0f);
        sampleTruck.setEstadoOperativo("Disponible");
    }

    @Test
    void getAllTrucks_shouldReturnList() throws Exception {
        when(camionRepository.findAll()).thenReturn(Arrays.asList(sampleTruck));
        mockMvc.perform(get("/api/camiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patente").value("AB-CD-12"));
    }

    @Test
    void getTruckById_notFound_shouldReturn404() throws Exception {
        when(camionRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/camiones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTruck_shouldReturnCreated() throws Exception {
        when(camionRepository.save(any(Truck.class))).thenReturn(sampleTruck);
        mockMvc.perform(post("/api/camiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTruck)))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteTruck_shouldReturnNoContent() throws Exception {
        doNothing().when(camionRepository).deleteById(1);
        mockMvc.perform(delete("/api/camiones/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getTruckById_shouldReturnTruck() throws Exception {
        when(camionRepository.findById(1)).thenReturn(Optional.of(sampleTruck));
        mockMvc.perform(get("/api/camiones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patente").value("AB-CD-12"));
    }

    @Test
    void updateTruck_shouldReturnUpdated() throws Exception {
        when(camionRepository.findById(1)).thenReturn(Optional.of(sampleTruck));
        when(camionRepository.save(any(Truck.class))).thenReturn(sampleTruck);
        mockMvc.perform(put("/api/camiones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTruck)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTruck_notFound_shouldReturn404() throws Exception {
        when(camionRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/camiones/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTruck)))
                .andExpect(status().isNotFound());
    }
}