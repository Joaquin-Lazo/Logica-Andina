package com.routes.route_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "rutas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Integer idRuta;

    @Column(name = "id_conductor_ref", nullable = false)
    private Integer idConductorRef;

    @Column(name = "id_despachador_ref", nullable = false)
    private Integer idDespachadorRef;

    @ManyToOne
    @JoinColumn(name = "id_camion", nullable = false)
    private Truck truck;

    @Column(name = "origen_direccion", nullable = false, length = 255)
    private String origenDireccion;

    @Column(name = "destino_direccion", nullable = false, length = 255)
    private String destinoDireccion;

    @Column(name = "lat_destino", nullable = false)
    private Double latDestino;

    @Column(name = "lng_destino", nullable = false)
    private Double lngDestino;

    @Column(name = "distancia_estimada_km", nullable = false)
    private Float distanciaEstimadaKm;

    @Column(name = "estado", length = 50)
    private String estado;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_salida_real")
    private LocalDateTime fechaSalidaReal;

    @Column(name = "eta_calculado")
    private LocalDateTime etaCalculado;
}