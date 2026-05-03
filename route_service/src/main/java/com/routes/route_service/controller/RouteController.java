package com.routes.route_service.controller;

import com.routes.route_service.model.Route;
import com.routes.route_service.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteRepository routeRepository;

    @GetMapping
    public ResponseEntity<List<Route>> getAllRoutes() {
        return new ResponseEntity<>(routeRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable Integer id) {
        Optional<Route> route = routeRepository.findById(id);
        return route.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Route> createRoute(@RequestBody Route newRoute) {
        try {
            Route savedRoute = routeRepository.save(newRoute);
            return new ResponseEntity<>(savedRoute, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Integer id, @RequestBody Route updatedRouteData) {
        Optional<Route> existingRoute = routeRepository.findById(id);
        if (existingRoute.isPresent()) {
            Route routeToUpdate = existingRoute.get();
            
            routeToUpdate.setIdConductorRef(updatedRouteData.getIdConductorRef());
            routeToUpdate.setIdDespachadorRef(updatedRouteData.getIdDespachadorRef());
            routeToUpdate.setTruck(updatedRouteData.getTruck());
            routeToUpdate.setOrigenDireccion(updatedRouteData.getOrigenDireccion());
            routeToUpdate.setDestinoDireccion(updatedRouteData.getDestinoDireccion());
            routeToUpdate.setLatDestino(updatedRouteData.getLatDestino());
            routeToUpdate.setLngDestino(updatedRouteData.getLngDestino());
            routeToUpdate.setDistanciaEstimadaKm(updatedRouteData.getDistanciaEstimadaKm());
            routeToUpdate.setEstado(updatedRouteData.getEstado());
            routeToUpdate.setFechaSalidaReal(updatedRouteData.getFechaSalidaReal());
            routeToUpdate.setEtaCalculado(updatedRouteData.getEtaCalculado());
            
            return new ResponseEntity<>(routeRepository.save(routeToUpdate), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteRoute(@PathVariable Integer id) {
        try {
            routeRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}