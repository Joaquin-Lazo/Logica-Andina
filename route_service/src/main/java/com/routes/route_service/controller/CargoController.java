package com.routes.route_service.controller;

import com.routes.route_service.model.Cargo;
import com.routes.route_service.repository.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cargo")
public class CargoController {

    @Autowired
    private CargoRepository cargoRepository;

    @GetMapping
    public ResponseEntity<List<Cargo>> getAllCargo() {
        return new ResponseEntity<>(cargoRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cargo> getCargoById(@PathVariable Integer id) {
        Optional<Cargo> cargo = cargoRepository.findById(id);
        return cargo.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Cargo> createCargo(@RequestBody Cargo newCargo) {
        try {
            Cargo savedCargo = cargoRepository.save(newCargo);
            return new ResponseEntity<>(savedCargo, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cargo> updateCargo(@PathVariable Integer id, @RequestBody Cargo updatedCargoData) {
        Optional<Cargo> existingCargo = cargoRepository.findById(id);
        if (existingCargo.isPresent()) {
            Cargo cargoToUpdate = existingCargo.get();
            
            cargoToUpdate.setRoute(updatedCargoData.getRoute());
            cargoToUpdate.setClient(updatedCargoData.getClient());
            cargoToUpdate.setDescripcionProductos(updatedCargoData.getDescripcionProductos());
            cargoToUpdate.setTipoCarga(updatedCargoData.getTipoCarga());
            cargoToUpdate.setPesoToneladas(updatedCargoData.getPesoToneladas());
            cargoToUpdate.setVolumenM3(updatedCargoData.getVolumenM3());
            cargoToUpdate.setEstadoEntrega(updatedCargoData.getEstadoEntrega());
            
            return new ResponseEntity<>(cargoRepository.save(cargoToUpdate), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCargo(@PathVariable Integer id) {
        try {
            cargoRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}