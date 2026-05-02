package com.routes.route_service.controller;

import com.routes.route_service.model.Cargo;
import com.routes.route_service.repository.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargo")
public class CargoController {

    @Autowired
    private CargoRepository cargoRepository;

    @GetMapping
    public List<Cargo> getAllCargo() {
        return cargoRepository.findAll();
    }
}