package com.telemetry.telemetry_service.dto;

public record TelemetryLogDTO(
    Integer idRutaRef,
    Double latitud,
    Double longitud,
    Float velocidadKmh,
    java.time.LocalDateTime timestampEvento
) {}
