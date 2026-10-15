package com.example.backendgymteo1.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle individual de un socio con estado y vigencia de membresía")
public class MiembroEstadoDetalleDto {

    @Schema(description = "ID del socio", example = "10")
    private Integer socioId;

    @Schema(description = "DPI o identificación del socio", example = "2987123450101")
    private String dpi;

    @Schema(description = "Nombres del socio", example = "Carlos")
    private String nombres;

    @Schema(description = "Apellidos del socio", example = "López")
    private String apellidos;

    @Schema(description = "Nombre completo del socio", example = "Carlos López")
    private String nombreCompleto;

    @Schema(description = "Correo electrónico", example = "carlos@gym.com")
    private String correo;

    @Schema(description = "Teléfono de contacto", example = "+502 5555-1234")
    private String telefono;

    @Schema(description = "ID de la sucursal asignada", example = "1")
    private Integer sucursalId;

    @Schema(description = "Nombre de la sucursal asignada", example = "Central")
    private String sucursalNombre;

    @Schema(description = "Fecha en que se registró en el gimnasio", example = "2026-01-15")
    private LocalDate fechaRegistro;

    @Schema(description = "Estado actual del socio frente a sus pagos: ACTIVO o MOROSO", example = "ACTIVO")
    private String estado;

    @Schema(description = "ID de la membresía activa o última registrada", example = "42")
    private Integer membresiaId;

    @Schema(description = "Nombre del plan de membresía actual o más reciente", example = "Plan Anual VIP")
    private String planNombre;

    @Schema(description = "Fecha de inicio de la membresía", example = "2026-01-15")
    private LocalDate fechaInicioMembresia;

    @Schema(description = "Fecha de vencimiento de la membresía", example = "2026-11-15")
    private LocalDate fechaVencimientoMembresia;

    @Schema(description = "Días restantes de vigencia de la membresía activa", example = "31")
    private Long diasRestantes;

    @Schema(description = "Días en mora o retraso si está vencido", example = "0")
    private Long diasMora;

    @Schema(description = "Fecha de la última asistencia registrada al gimnasio", example = "2026-10-14")
    private LocalDate ultimaAsistencia;
}
