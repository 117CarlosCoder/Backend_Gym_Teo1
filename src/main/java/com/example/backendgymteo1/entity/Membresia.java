package com.example.backendgymteo1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "membresia")
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_membresia")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_socio", nullable = false)
    private Socio socio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanMembresia plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_membresia", nullable = false)
    private EstadoMembresia estadoMembresia;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "membresia_sucursal",
            joinColumns = @JoinColumn(name = "id_membresia"),
            inverseJoinColumns = @JoinColumn(name = "id_sucursal")
    )
    @ToString.Exclude
    private Set<Sucursal> sucursales = new HashSet<>();

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cancelo")
    private User usuarioCancelo;

    @Column(name = "motivo_cancelacion", length = 100)
    private String motivoCancelacion;

    @Column(name = "comentarios_cancelacion", columnDefinition = "TEXT")
    private String comentariosCancelacion;

    @Builder.Default
    @Column(name = "reembolso", nullable = false)
    private boolean reembolso = false;

    @Column(name = "monto_reembolso", precision = 10, scale = 2)
    private BigDecimal montoReembolso;
}
