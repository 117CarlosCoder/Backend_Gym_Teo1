package com.example.backendgymteo1.dto.socio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resumen de la membresía actual del socio")
public class MembresiaResumenDto {

    @Schema(description = "ID de la membresía", example = "5")
    private Integer idMembresia;

    @Schema(description = "Nombre del plan contratado", example = "Plan Mensual")
    private String plan;

    @Schema(description = "Estado de la membresía", example = "ACTIVA")
    private String estado;

    @Schema(description = "Fecha de inicio de vigencia", example = "2026-09-01")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha de vencimiento", example = "2026-10-01")
    private LocalDate fechaVencimiento;

    @Schema(description = "Indica si está vigente actualmente", example = "true")
    private boolean activa;

    @Schema(description = "Días restantes de membresía", example = "27")
    private Long diasRestantes;
}
