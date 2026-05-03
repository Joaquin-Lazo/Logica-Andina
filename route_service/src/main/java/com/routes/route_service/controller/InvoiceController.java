package com.routes.route_service.controller;

import com.routes.route_service.model.Invoice;
import com.routes.route_service.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return new ResponseEntity<>(invoiceRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Integer id) {
        Optional<Invoice> invoice = invoiceRepository.findById(id);
        return invoice.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice newInvoice) {
        try {
            Invoice savedInvoice = invoiceRepository.save(newInvoice);
            return new ResponseEntity<>(savedInvoice, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Invoice> updateInvoice(@PathVariable Integer id, @RequestBody Invoice updatedInvoiceData) {
        Optional<Invoice> existingInvoice = invoiceRepository.findById(id);
        if (existingInvoice.isPresent()) {
            Invoice invoiceToUpdate = existingInvoice.get();
            
            invoiceToUpdate.setRoute(updatedInvoiceData.getRoute());
            invoiceToUpdate.setClient(updatedInvoiceData.getClient());
            invoiceToUpdate.setMontoNeto(updatedInvoiceData.getMontoNeto());
            invoiceToUpdate.setImpuestos(updatedInvoiceData.getImpuestos());
            invoiceToUpdate.setTotalPagar(updatedInvoiceData.getTotalPagar());
            invoiceToUpdate.setEstadoPago(updatedInvoiceData.getEstadoPago());
            
            return new ResponseEntity<>(invoiceRepository.save(invoiceToUpdate), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteInvoice(@PathVariable Integer id) {
        try {
            invoiceRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}