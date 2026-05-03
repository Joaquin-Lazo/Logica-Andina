package com.bff.bff_service.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSummaryDTO {
    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private RoleDTO rol;
    private String rut;
    private String telefono;
    private Boolean estadoActivo;
}