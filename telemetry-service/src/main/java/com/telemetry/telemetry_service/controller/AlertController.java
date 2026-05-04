package com.telemetry.telemetry_service.controller;

import com.telemetry.telemetry_service.model.Alert;
import com.telemetry.telemetry_service.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertRepository alertRepository;

    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return new ResponseEntity<>(alertRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Alert>> getActiveAlerts() {
        return new ResponseEntity<>(alertRepository.findByResueltaFalse(), HttpStatus.OK);
    }
}
