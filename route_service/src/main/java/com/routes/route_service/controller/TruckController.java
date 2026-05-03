package com.routes.route_service.controller;

import com.routes.route_service.model.Truck;
import com.routes.route_service.repository.TruckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/camiones")
public class TruckController {

    @Autowired
    private TruckRepository camionRepository;

    @GetMapping
    public ResponseEntity<List<Truck>> getAllCamiones() {
        return new ResponseEntity<>(camionRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Truck> getTruckById(@PathVariable Integer id) {
        Optional<Truck> truck = camionRepository.findById(id);
        return truck.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Truck> createTruck(@RequestBody Truck newTruck) {
        try {
            Truck savedTruck = camionRepository.save(newTruck);
            return new ResponseEntity<>(savedTruck, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Truck> updateTruck(@PathVariable Integer id, @RequestBody Truck updatedTruckData) {
        Optional<Truck> existingTruck = camionRepository.findById(id);
        if (existingTruck.isPresent()) {
            Truck truckToUpdate = existingTruck.get();
            
            truckToUpdate.setPatente(updatedTruckData.getPatente());
            truckToUpdate.setMarcaModelo(updatedTruckData.getMarcaModelo());
            truckToUpdate.setCapacidadMaxToneladas(updatedTruckData.getCapacidadMaxToneladas());
            truckToUpdate.setEstadoOperativo(updatedTruckData.getEstadoOperativo());
            
            return new ResponseEntity<>(camionRepository.save(truckToUpdate), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTruck(@PathVariable Integer id) {
        try {
            camionRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}