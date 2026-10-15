package com.example.backendgymteo1.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen de ingresos recaudados en un mes específico")
public class IngresoMensualDto {

    @Schema(description = "Año del reporte", example = "2026")
    private Integer anio;

    @Schema(description = "Número del mes (1-12)", example = "10")
    private Integer mes;

    @Schema(description = "Nombre legible del mes", example = "Octubre")
    private String nombreMes;

    @Schema(description = "Total recaudado en el mes", example = "4500.00")
    private BigDecimal total;

    @Schema(description = "Cantidad total de pagos procesados", example = "18")
    private Long cantidadPagos;
}
