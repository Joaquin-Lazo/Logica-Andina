package com.routes.route_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Integer idFactura;

    @ManyToOne
    @JoinColumn(name = "id_ruta", nullable = false)
    private Route route;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Client client;

    @Column(name = "monto_neto", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoNeto;

    @Column(name = "impuestos", nullable = false, precision = 12, scale = 2)
    private BigDecimal impuestos;

    @Column(name = "total_pagar", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPagar;

    @Column(name = "estado_pago", length = 50)
    private String estadoPago;

    @Column(name = "fecha_emision", insertable = false, updatable = false)
    private LocalDateTime fechaEmision;
}