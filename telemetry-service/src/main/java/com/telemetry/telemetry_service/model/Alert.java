package com.telemetry.telemetry_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_log_gps", nullable = false)
    private TelemetryLog logGps;

    @Column(name = "tipo_alerta", nullable = false, length = 100)
    private String tipoAlerta;

    @Column(name = "nivel_severidad", nullable = false, length = 50)
    private String nivelSeveridad;

    @Column(name = "resuelta")
    private Boolean resuelta = false;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "comentarios_despachador", columnDefinition = "TEXT")
    private String comentariosDespachador;
}
