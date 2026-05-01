package com.routes.route_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "rut_empresa", nullable = false, unique = true, length = 20)
    private String rutEmpresa;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "direccion_facturacion", nullable = false, length = 255)
    private String direccionFacturacion;

    @Column(name = "correo_contacto", nullable = false, length = 150)
    private String correoContacto;
}