package com.example.backendgymteo1.dto.promocion;

import com.example.backendgymteo1.entity.Promocion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para la creación de una promoción de descuento")
public class CreatePromocionDto {

    @NotBlank(message = "El nombre de la promoción es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Schema(description = "Nombre único de la promoción", example = "Promo Fin de Año")
    private String nombre;

    @Schema(description = "Descripción de la promoción", example = "10% de descuento en cualquier plan")
    private String descripcion;

    @NotNull(message = "El tipo de descuento es obligatorio")
    @Schema(description = "Tipo de descuento: PORCENTAJE o MONTO_FIJO", example = "PORCENTAJE")
    private Promocion.TipoDescuento tipoDescuento;

    @NotNull(message = "El valor del descuento es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor del descuento debe ser mayor a cero")
    @Schema(description = "Porcentaje (0-100) o monto fijo en quetzales según el tipo", example = "10.00")
    private BigDecimal valorDescuento;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Schema(description = "Primer día de vigencia", example = "2026-12-01")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Schema(description = "Último día de vigencia (inclusive)", example = "2026-12-31")
    private LocalDate fechaFin;

    @Schema(description = "Si la promoción está habilitada (por defecto true)", example = "true")
    private Boolean activo;
}
