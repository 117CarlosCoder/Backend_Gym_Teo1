package com.example.backendgymteo1.dto.membresia;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Congelamiento de una membresía")
public class CongelamientoResponseDto {

    @Schema(description = "ID del congelamiento", example = "3")
    private Integer id;

    @Schema(description = "ID de la membresía", example = "12")
    private Integer idMembresia;

    @Schema(description = "Plan de la membresía", example = "Plan Mensual")
    private String nombrePlan;

    @Schema(description = "Primer día congelado", example = "2026-11-01")
    private LocalDate fechaInicio;

    @Schema(description = "Último día congelado", example = "2026-11-15")
    private LocalDate fechaFin;

    @Schema(description = "Días sumados al vencimiento", example = "15")
    private Integer dias;

    @Schema(description = "Motivo", example = "Viaje de trabajo")
    private String motivo;

    @Schema(description = "PROGRAMADO, ACTIVO, FINALIZADO o ANULADO", example = "ACTIVO")
    private String estado;

    @Schema(description = "Fecha de vencimiento de la membresía después del congelamiento", example = "2026-12-15")
    private LocalDate fechaVencimientoMembresia;

    @Schema(description = "Fecha en que se registró", example = "2026-10-29T10:15:00")
    private LocalDateTime fechaRegistro;

    @Schema(description = "Usuario que lo registró", example = "María Castro")
    private String registradoPor;
}
