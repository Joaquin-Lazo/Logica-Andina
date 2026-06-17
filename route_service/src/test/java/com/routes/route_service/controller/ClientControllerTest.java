package com.routes.route_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routes.route_service.model.Client;
import com.routes.route_service.repository.ClientRepository;
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

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ClientRepository clientRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Client sampleClient;

    @BeforeEach
    void setUp() {
        sampleClient = new Client();
        sampleClient.setIdCliente(1);
        sampleClient.setRutEmpresa("11111111-K");
        sampleClient.setRazonSocial("Minera Norte SpA");
        sampleClient.setDireccionFacturacion("Av. Industrial 400, Antofagasta");
        sampleClient.setCorreoContacto("pagos@mineranorte.cl");
    }

    @Test
    void getAllClients_shouldReturnList() throws Exception {
        when(clientRepository.findAll()).thenReturn(Arrays.asList(sampleClient));
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].razonSocial").value("Minera Norte SpA"));
    }

    @Test
    void getClientById_notFound_shouldReturn404() throws Exception {
        when(clientRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/clients/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createClient_shouldReturnCreated() throws Exception {
        when(clientRepository.save(any(Client.class))).thenReturn(sampleClient);
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleClient)))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteClient_shouldReturnNoContent() throws Exception {
        doNothing().when(clientRepository).deleteById(1);
        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getClientById_shouldReturnClient() throws Exception {
        when(clientRepository.findById(1)).thenReturn(Optional.of(sampleClient));
        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razonSocial").value("Minera Norte SpA"));
    }

    @Test
    void updateClient_shouldReturnUpdated() throws Exception {
        when(clientRepository.findById(1)).thenReturn(Optional.of(sampleClient));
        when(clientRepository.save(any(Client.class))).thenReturn(sampleClient);
        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleClient)))
                .andExpect(status().isOk());
    }

    @Test
    void updateClient_notFound_shouldReturn404() throws Exception {
        when(clientRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/clients/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleClient)))
                .andExpect(status().isNotFound());
    }
}