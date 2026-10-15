package com.example.backendgymteo1.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dashboard con indicadores clave de rendimiento (KPIs)")
public class DashboardResumenDto {

    @Schema(description = "Ingresos recaudados en el mes en curso", example = "12500.00")
    private BigDecimal ingresosMesActual;

    @Schema(description = "Ingresos recaudados en el mes anterior", example = "11000.00")
    private BigDecimal ingresosMesAnterior;

    @Schema(description = "Porcentaje de crecimiento comparativo mensual (%)", example = "13.64")
    private Double crecimientoIngresosPorcentaje;

    @Schema(description = "Total de socios al día con membresía activa", example = "180")
    private Long totalSociosActivos;

    @Schema(description = "Total de socios morosos", example = "45")
    private Long totalSociosMorosos;

    @Schema(description = "Membresías activas que vencen en 7 días", example = "8")
    private Long membresiasPorVencer7Dias;

    @Schema(description = "Asistencias registradas hoy", example = "65")
    private Long asistenciasHoy;

    @Schema(description = "Asistencias acumuladas en el mes en curso", example = "1420")
    private Long asistenciasMesActual;

    @Schema(description = "Socios con membresías próximas a vencer")
    private List<MembresiaPorVencerDto> proximosVencimientos;

    @Schema(description = "Últimas asistencias registradas en el día")
    private List<AsistenciaResumenDto> ultimasAsistencias;
}
