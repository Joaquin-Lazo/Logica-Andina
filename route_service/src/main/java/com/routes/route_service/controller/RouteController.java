package com.routes.route_service.controller;

import com.routes.route_service.model.Route;
import com.routes.route_service.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteRepository routeRepository;

    @GetMapping
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }
}