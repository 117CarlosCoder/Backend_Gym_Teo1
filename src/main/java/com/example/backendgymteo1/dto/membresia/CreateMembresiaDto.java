package com.example.backendgymteo1.dto.membresia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para crear/adquirir una nueva membresía para un socio")
public class CreateMembresiaDto {

    @Schema(description = "ID del socio al que se le asignará la membresía", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del socio es obligatorio")
    private Integer idSocio;

    @Schema(description = "ID del plan de membresía seleccionado", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del plan es obligatorio")
    private Integer idPlan;

    @Schema(description = "Fecha de inicio de la membresía (por defecto la fecha actual si se omite)", example = "2026-03-01")
    private LocalDate fechaInicio;

    @Schema(description = "ID del estado de la membresía (por defecto 1 = ACTIVA)", example = "1")
    private Integer idEstadoMembresia;
}
