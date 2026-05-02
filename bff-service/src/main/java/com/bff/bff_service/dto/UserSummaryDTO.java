package com.bff.bff_service.dto;
import lombok.Data;

@Data
public class UserSummaryDTO {
    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private RoleDTO rol;
}