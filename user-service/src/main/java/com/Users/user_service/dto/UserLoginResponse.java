package com.Users.user_service.dto;

public record UserLoginResponse(
    String token,
    Integer idUsuario,
    String nombres,
    String apellidos,
    String correo,
    String rol
){
}