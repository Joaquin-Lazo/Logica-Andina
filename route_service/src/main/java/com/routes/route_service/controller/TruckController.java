package com.routes.route_service.controller;

import com.routes.route_service.model.Truck;
import com.routes.route_service.repository.TruckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/camiones")
public class TruckController {

    @Autowired
    private TruckRepository camionRepository;

    @GetMapping
    public List<Truck> getAllCamiones() {
        return camionRepository.findAll();
    }
}