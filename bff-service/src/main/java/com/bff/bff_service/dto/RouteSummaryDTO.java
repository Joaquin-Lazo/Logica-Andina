package com.bff.bff_service.dto;
import lombok.Data;

@Data
public class RouteSummaryDTO {
    private Integer idRuta;
    private String origenDireccion;
    private String destinoDireccion;
    private String estado;
    private TruckSummaryDTO truck;
}