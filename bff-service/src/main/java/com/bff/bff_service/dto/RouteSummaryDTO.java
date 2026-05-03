package com.bff.bff_service.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteSummaryDTO {
    private Integer idRuta;
    private String origenDireccion;
    private String destinoDireccion;
    private String estado;
    private TruckSummaryDTO truck;
    private Float distanciaEstimadaKm;
    private Integer idConductorRef;
    private Integer idDespachadorRef;
    private Double latDestino;
    private Double lngDestino;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaSalidaReal;
    private LocalDateTime etaCalculado;

    // Campos de simulacion (calculados por el BFF, no provienen de la BD)
    private Double progressPercent;
    private Double kmRecorridos;
    private Double velocidadSimulada;
}