package com.bff.bff_service.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TruckSummaryDTO {
    private Integer idCamion;
    private String patente;
    private String marcaModelo;
    private Float capacidadMaxToneladas;
    private String estadoOperativo;
}