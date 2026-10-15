package com.example.backendgymteo1.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte estadístico y detallado de socios activos frente a morosos")
public class ReporteMiembrosEstadoDto {

    @Schema(description = "Total de socios registrados y vigentes en el sistema", example = "250")
    private Long totalSocios;

    @Schema(description = "Socios con membresía activa y al día", example = "190")
    private Long activos;

    @Schema(description = "Socios con membresía vencida o sin membresía vigente (morosos)", example = "60")
    private Long morosos;

    @Schema(description = "Porcentaje de socios activos", example = "76.0")
    private Double porcentajeActivos;

    @Schema(description = "Porcentaje de socios morosos", example = "24.0")
    private Double porcentajeMorosos;

    @Schema(description = "Distribución de socios activos según plan de membresía")
    private List<DistribucionPlanDto> distribucionPorPlan;

    @Schema(description = "Distribución y comparativa por sucursal")
    private List<DistribucionSucursalDto> distribucionPorSucursal;

    @Schema(description = "Listado detallado de miembros con su información personal y estado de vigencia")
    private List<MiembroEstadoDetalleDto> miembros;
}
