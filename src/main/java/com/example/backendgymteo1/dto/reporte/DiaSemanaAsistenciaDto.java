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
@Schema(description = "Distribución de asistencia según el día de la semana")
public class DiaSemanaAsistenciaDto {

    @Schema(description = "Número del día (1 = Lunes, 7 = Domingo)", example = "1")
    private Integer numeroDia;

    @Schema(description = "Nombre del día", example = "Lunes")
    private String nombreDia;

    @Schema(description = "Cantidad total de asistencias", example = "120")
    private Long cantidad;

    @Schema(description = "Porcentaje respecto a la semana", example = "22.5")
    private Double porcentaje;
}
