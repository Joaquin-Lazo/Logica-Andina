package com.telemetry.telemetry_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs_telemetria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "id_ruta_ref", nullable = false)
    private Integer idRutaRef;

    @Column(name = "latitud", nullable = false, precision = 10, scale = 8)
    private Double latitud;

    @Column(name = "longitud", nullable = false, precision = 11, scale = 8)
    private Double longitud;

    @Column(name = "velocidad_kmh", nullable = false)
    private Float velocidadKmh;

    @Column(name = "timestamp_evento")
    private LocalDateTime timestampEvento;

    @PrePersist
    public void prePersist() {
        if (timestampEvento == null) timestampEvento = LocalDateTime.now();
    }
}
