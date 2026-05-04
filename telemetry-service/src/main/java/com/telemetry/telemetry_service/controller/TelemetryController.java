package com.telemetry.telemetry_service.controller;

import com.telemetry.telemetry_service.model.TelemetryLog;
import com.telemetry.telemetry_service.repository.TelemetryLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    @Autowired
    private TelemetryLogRepository logRepository;

    @GetMapping
    public ResponseEntity<List<TelemetryLog>> getAllLogs() {
        return new ResponseEntity<>(logRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<TelemetryLog>> getLogsByRoute(@PathVariable Integer routeId) {
        return new ResponseEntity<>(logRepository.findByIdRutaRefOrderByTimestampEventoDesc(routeId), HttpStatus.OK);
    }
}
