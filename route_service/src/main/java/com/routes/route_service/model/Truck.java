package com.routes.route_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "camiones") 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_camion")
    private Integer idCamion;

    @Column(name = "patente", nullable = false, unique = true, length = 15)
    private String patente;

    @Column(name = "marca_modelo", nullable = false, length = 100)
    private String marcaModelo;

    @Column(name = "capacidad_max_toneladas", nullable = false)
    private Float capacidadMaxToneladas;

    @Column(name = "estado_operativo", length = 50)
    private String estadoOperativo;
}