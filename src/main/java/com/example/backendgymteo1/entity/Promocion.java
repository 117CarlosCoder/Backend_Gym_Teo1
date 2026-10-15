package com.example.backendgymteo1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "promocion")
public class Promocion {

    public enum TipoDescuento {
        PORCENTAJE,
        MONTO_FIJO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion")
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false)
    private TipoDescuento tipoDescuento;

    @Column(name = "valor_descuento", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDescuento;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public BigDecimal calcularDescuento(BigDecimal precioBase) {
        if (precioBase == null || valorDescuento == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal descuento = tipoDescuento == TipoDescuento.PORCENTAJE
                ? precioBase.multiply(valorDescuento).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP)
                : valorDescuento.setScale(2, java.math.RoundingMode.HALF_UP);
        return descuento.min(precioBase);
    }

    public boolean estaVigente(LocalDate fecha) {
        return activo && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }
}
