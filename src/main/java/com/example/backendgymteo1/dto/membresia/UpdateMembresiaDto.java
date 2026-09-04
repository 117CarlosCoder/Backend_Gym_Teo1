package com.example.backendgymteo1.dto.membresia;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para actualizar una membresía (tipo de membresía, sucursales y fecha de vencimiento)")
public class UpdateMembresiaDto {

    @Schema(description = "Nuevo ID del plan / tipo de membresía (opcional)", example = "2")
    private Integer tipoMembresiaId;

    @Schema(description = "Nueva lista de IDs de sucursales autorizadas (permite agregar/quitar)", example = "[1, 2, 3]")
    private List<Integer> sucursalIds;

    @Schema(description = "Nueva fecha de vencimiento de la membresía (opcional)", example = "2026-11-30")
    private LocalDate fechaVencimiento;
}
