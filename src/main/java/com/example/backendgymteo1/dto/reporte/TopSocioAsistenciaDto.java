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
@Schema(description = "Socio con mayor concurrencia o asistencia")
public class TopSocioAsistenciaDto {

    @Schema(description = "ID del socio", example = "5")
    private Integer socioId;

    @Schema(description = "Nombre completo del socio", example = "Diego Pineda")
    private String nombreCompleto;

    @Schema(description = "Número total de asistencias en el período", example = "24")
    private Long asistencias;
}
