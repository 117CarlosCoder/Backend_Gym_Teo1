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
@Schema(description = "Conteo mensual de visitas del socio")
public class VisitasMesDto {

    @Schema(description = "Año", example = "2026")
    private Integer anio;

    @Schema(description = "Mes (1-12)", example = "10")
    private Integer mes;

    @Schema(description = "Nombre del mes", example = "Octubre")
    private String nombreMes;

    @Schema(description = "Número de visitas realizadas en ese mes", example = "16")
    private Long visitas;
}
