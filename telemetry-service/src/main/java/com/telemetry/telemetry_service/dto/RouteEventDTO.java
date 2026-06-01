package com.telemetry.telemetry_service.dto;

import java.io.Serializable;

public record RouteEventDTO(
        Integer idRuta,
        String nuevoEstado,
        Integer idConductorRef,
        Double latDestino,
        Double lngDestino,
        Double distanciaEstimadaKm,
        String origenDireccion,
        String destinoDireccion) implements Serializable {
}