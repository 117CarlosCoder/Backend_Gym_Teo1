package com.example.backendgymteo1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Periodo en que una membresía queda congelada. Los días congelados se suman a su vencimiento,
 * de modo que el socio no pierde tiempo pagado.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "congelamiento_membresia")
public class CongelamientoMembresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_congelamiento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_membresia", nullable = false)
    @ToString.Exclude
    private Membresia membresia;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    /** Días que se sumaron al vencimiento de la membresía. */
    @Column(name = "dias", nullable = false)
    private Integer dias;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    /** true si se canceló antes de empezar (no consumió días). */
    @Builder.Default
    @Column(name = "anulado", nullable = false)
    private boolean anulado = false;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "registrado_por", length = 150)
    private String registradoPor;

    public boolean estaActivoEn(LocalDate fecha) {
        return !anulado && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    /** PROGRAMADO, ACTIVO, FINALIZADO o ANULADO según la fecha indicada. */
    public String estadoEn(LocalDate fecha) {
        if (anulado) return "ANULADO";
        if (fecha.isBefore(fechaInicio)) return "PROGRAMADO";
        if (fecha.isAfter(fechaFin)) return "FINALIZADO";
        return "ACTIVO";
    }
}
