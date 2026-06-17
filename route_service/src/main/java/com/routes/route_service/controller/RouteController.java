package com.routes.route_service.controller;

import com.routes.route_service.dto.RouteEventDTO;
import com.routes.route_service.model.Route;
import com.routes.route_service.repository.RouteRepository;
import com.routes.route_service.service.RouteEventProducer;

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

    @Autowired
    private RouteEventProducer routeEventProducer;

    @Autowired
    private com.routes.route_service.repository.CargoRepository cargoRepository;

    @Autowired
    private com.routes.route_service.repository.InvoiceRepository invoiceRepository;

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

    @PutMapping("/{id}/status")
    public ResponseEntity<Route> updateRouteStatus(@PathVariable Integer id,
            @RequestBody java.util.Map<String, String> body) {
        Optional<Route> existingRoute = routeRepository.findById(id);
        if (existingRoute.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String nuevoEstado = body.get("estado");
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Route route = existingRoute.get();
        String oldEstado = route.getEstado();
        route.setEstado(nuevoEstado);
        if ("En Transito".equalsIgnoreCase(nuevoEstado) && route.getFechaSalidaReal() == null) {
            route.setFechaSalidaReal(java.time.LocalDateTime.now());
        }
        Route saved = routeRepository.save(route);
        if (!saved.getEstado().equals(oldEstado)) {
            publishStatusEvent(saved);
        }
        // Auto-update cargo and invoice when route completes
        if ("Completada".equalsIgnoreCase(nuevoEstado)) {
            // Mark all cargo for this route as "Entregado"
            cargoRepository.findAll().stream()
                .filter(c -> c.getRoute() != null && c.getRoute().getIdRuta().equals(id))
                .forEach(c -> {
                    c.setEstadoEntrega("Entregado");
                    cargoRepository.save(c);
                });
            // Mark all invoices for this route as "Pagada" 
            invoiceRepository.findAll().stream()
                .filter(inv -> inv.getRoute() != null && inv.getRoute().getIdRuta().equals(id))
                .forEach(inv -> {
                    inv.setEstadoPago("Pagada");
                    invoiceRepository.save(inv);
                });
        }
        
        return new ResponseEntity<>(saved, HttpStatus.OK);
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

    private void publishStatusEvent(Route route) {
        RouteEventDTO event = new RouteEventDTO(
                route.getIdRuta(), route.getEstado(), route.getIdConductorRef(),
                route.getLatDestino(), route.getLngDestino(),
                route.getDistanciaEstimadaKm() != null ? route.getDistanciaEstimadaKm().doubleValue() : 0.0,
                route.getOrigenDireccion(), route.getDestinoDireccion());
        routeEventProducer.publishRouteEvent(event);
    }
}