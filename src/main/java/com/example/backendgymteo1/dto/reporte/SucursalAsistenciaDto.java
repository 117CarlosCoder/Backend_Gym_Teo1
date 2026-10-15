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
@Schema(description = "Concurrencia de asistencias por sucursal")
public class SucursalAsistenciaDto {

    @Schema(description = "ID de la sucursal", example = "1")
    private Integer sucursalId;

    @Schema(description = "Nombre de la sucursal", example = "Sede Central")
    private String nombreSucursal;

    @Schema(description = "Total de ingresos registrados", example = "350")
    private Long cantidad;

    @Schema(description = "Porcentaje respecto al total global", example = "60.0")
    private Double porcentaje;
}
