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
@Schema(description = "Métricas mensuales de nuevas suscripciones y renovaciones")
public class RenovacionMesDto {

    @Schema(description = "Año", example = "2026")
    private Integer anio;

    @Schema(description = "Mes (1-12)", example = "10")
    private Integer mes;

    @Schema(description = "Nombre del mes", example = "Octubre")
    private String nombreMes;

    @Schema(description = "Nuevas altas de socios (primera membresía)", example = "12")
    private Long nuevas;

    @Schema(description = "Renovaciones de socios existentes", example = "28")
    private Long renovaciones;

    @Schema(description = "Total de membresías emitidas en el mes", example = "40")
    private Long total;

    @Schema(description = "Tasa porcentual de renovación", example = "70.0")
    private Double tasaRenovacion;
}
