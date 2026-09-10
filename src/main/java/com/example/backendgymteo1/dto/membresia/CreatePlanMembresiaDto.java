package com.example.backendgymteo1.dto.membresia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Datos requeridos para la creación de un nuevo plan de membresía")
public class CreatePlanMembresiaDto {

    @Schema(description = "Nombre identificador único del plan", example = "Plan Trimestral Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre del plan es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @Schema(description = "Duración del plan en días calendario", example = "90", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La duración en días es obligatoria")
    @Min(value = 1, message = "La duración mínima debe ser de al menos 1 día")
    private Integer duracion;

    @Schema(description = "Precio o tarifa regular del plan", example = "650.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener como máximo 8 enteros y 2 decimales")
    private BigDecimal precio;

    @Schema(description = "Descripción detallada de beneficios y alcance del plan", example = "Acceso ilimitado a todas las áreas y sucursales por 3 meses")
    private String descripcion;
}
