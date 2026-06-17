package com.routes.route_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routes.route_service.model.Cargo;
import com.routes.route_service.repository.CargoRepository;
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

@WebMvcTest(CargoController.class)
@AutoConfigureMockMvc(addFilters = false)
class CargoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CargoRepository cargoRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Cargo sampleCargo;

    @BeforeEach
    void setUp() {
        sampleCargo = new Cargo();
        sampleCargo.setIdCargamento(1);
        sampleCargo.setDescripcionProductos("Cobre");
        sampleCargo.setTipoCarga("Mineral");
        sampleCargo.setPesoToneladas(25.5f);
        sampleCargo.setVolumenM3(10.0f);
        sampleCargo.setEstadoEntrega("En Transito");
    }

    @Test
    void getAllCargo_shouldReturnList() throws Exception {
        when(cargoRepository.findAll()).thenReturn(Arrays.asList(sampleCargo));
        mockMvc.perform(get("/api/cargo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcionProductos").value("Cobre"));
    }

    @Test
    void getCargoById_shouldReturnCargo() throws Exception {
        when(cargoRepository.findById(1)).thenReturn(Optional.of(sampleCargo));
        mockMvc.perform(get("/api/cargo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCarga").value("Mineral"));
    }

    @Test
    void getCargoById_notFound_shouldReturn404() throws Exception {
        when(cargoRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/cargo/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCargo_shouldReturnCreated() throws Exception {
        when(cargoRepository.save(any(Cargo.class))).thenReturn(sampleCargo);
        mockMvc.perform(post("/api/cargo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCargo)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateCargo_shouldReturnUpdated() throws Exception {
        when(cargoRepository.findById(1)).thenReturn(Optional.of(sampleCargo));
        when(cargoRepository.save(any(Cargo.class))).thenReturn(sampleCargo);
        mockMvc.perform(put("/api/cargo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCargo)))
                .andExpect(status().isOk());
    }

    @Test
    void updateCargo_notFound_shouldReturn404() throws Exception {
        when(cargoRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/cargo/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCargo)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCargo_shouldReturnNoContent() throws Exception {
        doNothing().when(cargoRepository).deleteById(1);
        mockMvc.perform(delete("/api/cargo/1"))
                .andExpect(status().isNoContent());
    }
}
