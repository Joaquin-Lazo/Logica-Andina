package com.routes.route_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routes.route_service.model.Invoice;
import com.routes.route_service.repository.InvoiceRepository;
import com.routes.route_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceRepository invoiceRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Invoice sampleInvoice;

    @BeforeEach
    void setUp() {
        sampleInvoice = new Invoice();
        sampleInvoice.setIdFactura(1);
        sampleInvoice.setMontoNeto(new BigDecimal("1500000.00"));
        sampleInvoice.setImpuestos(new BigDecimal("285000.00"));
        sampleInvoice.setTotalPagar(new BigDecimal("1785000.00"));
        sampleInvoice.setEstadoPago("Pendiente");
    }

    @Test
    void getAllInvoices_shouldReturnList() throws Exception {
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(sampleInvoice));
        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estadoPago").value("Pendiente"));
    }

    @Test
    void getInvoiceById_notFound_shouldReturn404() throws Exception {
        when(invoiceRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/invoices/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteInvoice_shouldReturnNoContent() throws Exception {
        doNothing().when(invoiceRepository).deleteById(1);
        mockMvc.perform(delete("/api/invoices/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getInvoiceById_shouldReturnInvoice() throws Exception {
        when(invoiceRepository.findById(1)).thenReturn(Optional.of(sampleInvoice));
        mockMvc.perform(get("/api/invoices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPago").value("Pendiente"));
    }

    @Test
    void createInvoice_shouldReturnCreated() throws Exception {
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(sampleInvoice);
        mockMvc.perform(post("/api/invoices")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sampleInvoice)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateInvoice_shouldReturnUpdated() throws Exception {
        when(invoiceRepository.findById(1)).thenReturn(Optional.of(sampleInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(sampleInvoice);
        mockMvc.perform(put("/api/invoices/1")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sampleInvoice)))
                .andExpect(status().isOk());
    }

    @Test
    void updateInvoice_notFound_shouldReturn404() throws Exception {
        when(invoiceRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/invoices/99")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sampleInvoice)))
                .andExpect(status().isNotFound());
    }
}