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
@Schema(description = "Frecuencia de asistencias agrupadas por hora del día")
public class HoraPicoDto {

    @Schema(description = "Hora del día (formato 24h, 0-23)", example = "18")
    private Integer hora;

    @Schema(description = "Etiqueta legible del rango horario", example = "18:00 - 19:00")
    private String etiquetaHora;

    @Schema(description = "Cantidad total de check-ins registrados en esa hora", example = "42")
    private Long cantidad;
}
