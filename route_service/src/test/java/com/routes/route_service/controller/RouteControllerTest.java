package com.routes.route_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routes.route_service.model.Route;
import com.routes.route_service.model.Truck;
import com.routes.route_service.repository.RouteRepository;
import com.routes.route_service.security.JwtUtil;
import com.routes.route_service.service.RouteEventProducer;
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

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RouteRepository routeRepository;

    @MockitoBean
    private RouteEventProducer routeEventProducer;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private com.routes.route_service.repository.CargoRepository cargoRepository;

    @MockitoBean
    private com.routes.route_service.repository.InvoiceRepository invoiceRepository;

    private Route sampleRoute;
    private Truck sampleTruck;

    @BeforeEach
    void setUp() {
        sampleTruck = new Truck();
        sampleTruck.setIdCamion(1);
        sampleTruck.setPatente("AB-CD-12");
        sampleTruck.setMarcaModelo("Volvo FH16");
        sampleTruck.setCapacidadMaxToneladas(30.0f);
        sampleTruck.setEstadoOperativo("Disponible");

        sampleRoute = new Route();
        sampleRoute.setIdRuta(1);
        sampleRoute.setIdConductorRef(7);
        sampleRoute.setIdDespachadorRef(2);
        sampleRoute.setTruck(sampleTruck);
        sampleRoute.setOrigenDireccion("Santiago");
        sampleRoute.setDestinoDireccion("Antofagasta");
        sampleRoute.setLatDestino(-23.65);
        sampleRoute.setLngDestino(-70.4);
        sampleRoute.setDistanciaEstimadaKm(1335.2f);
        sampleRoute.setEstado("Pendiente");
    }

    @Test
    void getAllRoutes_shouldReturnList() throws Exception {
        when(routeRepository.findAll()).thenReturn(Arrays.asList(sampleRoute));
        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origenDireccion").value("Santiago"));
    }

    @Test
    void getRouteById_shouldReturnRoute() throws Exception {
        when(routeRepository.findById(1)).thenReturn(Optional.of(sampleRoute));
        mockMvc.perform(get("/api/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinoDireccion").value("Antofagasta"));
    }

    @Test
    void getRouteById_notFound_shouldReturn404() throws Exception {
        when(routeRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/routes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRoute_shouldReturnCreated() throws Exception {
        when(routeRepository.save(any(Route.class))).thenReturn(sampleRoute);
        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRoute)))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteRoute_shouldReturnNoContent() throws Exception {
        doNothing().when(routeRepository).deleteById(1);
        mockMvc.perform(delete("/api/routes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateRoute_shouldReturnUpdated() throws Exception {
        when(routeRepository.findById(1)).thenReturn(Optional.of(sampleRoute));
        when(routeRepository.save(any(Route.class))).thenReturn(sampleRoute);
        mockMvc.perform(put("/api/routes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRoute)))
                .andExpect(status().isOk());
    }

    @Test
    void updateRoute_notFound_shouldReturn404() throws Exception {
        when(routeRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/routes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRoute)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRouteStatus_shouldReturnUpdated() throws Exception {
        when(routeRepository.findById(1)).thenReturn(Optional.of(sampleRoute));
        when(routeRepository.save(any(Route.class))).thenReturn(sampleRoute);
        
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("estado", "En Transito");
        
        mockMvc.perform(put("/api/routes/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void updateRouteStatus_notFound_shouldReturn404() throws Exception {
        when(routeRepository.findById(99)).thenReturn(Optional.empty());
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("estado", "En Transito");
        mockMvc.perform(put("/api/routes/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void updateRouteStatus_badRequest_shouldReturn400() throws Exception {
        when(routeRepository.findById(1)).thenReturn(Optional.of(sampleRoute));
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("estado", "");
        mockMvc.perform(put("/api/routes/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}