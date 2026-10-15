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
@Schema(description = "Reporte analítico de retención y renovación de membresías")
public class ReporteRenovacionesDto {

    @Schema(description = "Año consultado o null si es histórico", example = "2026")
    private Integer anio;

    @Schema(description = "Total de membresías emitidas en el período", example = "350")
    private Long totalMembresiasPeriodo;

    @Schema(description = "Total de membresías nuevas", example = "120")
    private Long nuevasMembresias;

    @Schema(description = "Total de membresías renovadas", example = "230")
    private Long totalRenovaciones;

    @Schema(description = "Tasa global de renovación del período (%)", example = "65.71")
    private Double tasaRenovacionGlobal;

    @Schema(description = "Membresías activas que vencen en los próximos 7 días", example = "15")
    private Long proximasAVencerSiguienteSemana;

    @Schema(description = "Desglose cronológico mensual de renovaciones")
    private List<RenovacionMesDto> renovacionesPorMes;

    @Schema(description = "Listado de miembros con membresía próxima a vencer en los próximos 7 días")
    private List<MembresiaPorVencerDto> proximasAVencer;

    @Schema(description = "Últimas emisiones y renovaciones registradas en el período")
    private List<RenovacionDetalleDto> ultimasRenovaciones;
}
