package com.example.backendgymteo1.dto.membresia;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Detalle del plan de membresía")
public class PlanMembresiaResponseDto {

    @Schema(description = "Identificador del plan", example = "1")
    private Integer id;

    @Schema(description = "Nombre del plan", example = "Plan Mensual")
    private String nombre;

    @Schema(description = "Duración en días", example = "30")
    private Integer duracion;

    @Schema(description = "Precio del plan", example = "250.00")
    private BigDecimal precio;

    @Schema(description = "Descripción del plan", example = "Acceso completo a máquinas y áreas comunes")
    private String descripcion;
}
