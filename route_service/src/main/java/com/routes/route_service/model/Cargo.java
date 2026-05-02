package com.routes.route_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "cargamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cargamento")
    private Integer idCargamento;

    @ManyToOne
    @JoinColumn(name = "id_ruta", nullable = false)
    private Route route;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Client client;

    @Column(name = "descripcion_productos", nullable = false, columnDefinition = "TEXT")
    private String descripcionProductos;

    @Column(name = "tipo_carga", nullable = false, length = 50)
    private String tipoCarga;

    @Column(name = "peso_toneladas", nullable = false)
    private Float pesoToneladas;

    @Column(name = "volumen_m3", nullable = false)
    private Float volumenM3;

    @Column(name = "estado_entrega", length = 50)
    private String estadoEntrega;
}