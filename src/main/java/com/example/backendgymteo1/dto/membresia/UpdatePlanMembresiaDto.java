package com.example.backendgymteo1.dto.membresia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos opcionales para la actualización de un plan de membresía")
public class UpdatePlanMembresiaDto {

    @Schema(description = "Nuevo nombre identificador del plan", example = "Plan Trimestral Plus")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @Schema(description = "Nueva duración del plan en días", example = "90")
    @Min(value = 1, message = "La duración mínima debe ser de al menos 1 día")
    private Integer duracion;

    @Schema(description = "Nuevo precio del plan", example = "700.00")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener como máximo 8 enteros y 2 decimales")
    private BigDecimal precio;

    @Schema(description = "Nueva descripción del plan", example = "Acceso total a sucursales y clases grupales incluidas")
    private String descripcion;
}
