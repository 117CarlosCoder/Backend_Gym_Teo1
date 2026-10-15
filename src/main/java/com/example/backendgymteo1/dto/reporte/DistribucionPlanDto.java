package com.example.backendgymteo1.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Distribución de socios por plan de membresía")
public class DistribucionPlanDto {

    @Schema(description = "ID del plan de membresía", example = "1")
    private Integer planId;

    @Schema(description = "Nombre del plan", example = "Plan Anual VIP")
    private String nombrePlan;

    @Schema(description = "Cantidad de socios con este plan activo", example = "45")
    private Long cantidad;

    @Schema(description = "Porcentaje respecto al total de membresías activas", example = "37.5")
    private Double porcentaje;
}
