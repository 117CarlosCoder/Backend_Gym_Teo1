package com.example.backendgymteo1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recordatorio_correo")
public class RecordatorioCorreo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recordatorio_correo")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_notificacion", nullable = false)
    @ToString.Exclude
    private Notificacion notificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_membresia", nullable = false)
    @ToString.Exclude
    private Membresia membresia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @ToString.Exclude
    private User usuario;

    @Column(name = "destinatario", nullable = false, length = 150)
    private String destinatario;

    @Column(name = "nombre_socio_snapshot", nullable = false, length = 255)
    private String nombreSocioSnapshot;

    @Column(name = "nombre_plan_snapshot", nullable = false, length = 100)
    private String nombrePlanSnapshot;

    @Column(name = "dias_restantes_snapshot", nullable = false)
    private int diasRestantesSnapshot;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoRecordatorioCorreo estado = EstadoRecordatorioCorreo.PENDIENTE;

    @Builder.Default
    @Column(name = "intentos", nullable = false)
    private int intentos = 0;

    @Builder.Default
    @Column(name = "max_intentos", nullable = false)
    private int maxIntentos = 3;

    @Column(name = "proxima_ejecucion", nullable = false)
    private LocalDateTime proximaEjecucion;

    @Column(name = "ultimo_intento")
    private LocalDateTime ultimoIntento;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "claim_token", length = 36)
    private String claimToken;

    @Column(name = "idempotency_key", nullable = false, length = 120, unique = true)
    private String idempotencyKey;

    @Column(name = "proveedor_mensaje_id", length = 100)
    private String proveedorMensajeId;

    @Column(name = "error_mensaje", columnDefinition = "TEXT")
    private String errorMensaje;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.proximaEjecucion == null) {
            this.proximaEjecucion = LocalDateTime.now();
        }
    }
}
