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
@Schema(description = "Distribución de socios activos y morosos por sucursal")
public class DistribucionSucursalDto {

    @Schema(description = "ID de la sucursal", example = "1")
    private Integer sucursalId;

    @Schema(description = "Nombre de la sucursal", example = "Sede Central")
    private String nombreSucursal;

    @Schema(description = "Cantidad de socios activos al día", example = "80")
    private Long activos;

    @Schema(description = "Cantidad de socios morosos", example = "20")
    private Long morosos;

    @Schema(description = "Total de socios registrados en la sucursal", example = "100")
    private Long total;

    @Schema(description = "Porcentaje de solvencia en la sucursal", example = "80.0")
    private Double porcentajeSolvencia;
}
