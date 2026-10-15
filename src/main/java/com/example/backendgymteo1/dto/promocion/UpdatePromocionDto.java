package com.example.backendgymteo1.dto.promocion;

import com.example.backendgymteo1.entity.Promocion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
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
@Schema(description = "DTO para actualización parcial de una promoción (solo se aplican los campos enviados)")
public class UpdatePromocionDto {

    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Schema(description = "Nuevo nombre de la promoción", example = "Promo Enero")
    private String nombre;

    @Schema(description = "Nueva descripción")
    private String descripcion;

    @Schema(description = "Tipo de descuento: PORCENTAJE o MONTO_FIJO", example = "MONTO_FIJO")
    private Promocion.TipoDescuento tipoDescuento;

    @DecimalMin(value = "0.01", message = "El valor del descuento debe ser mayor a cero")
    @Schema(description = "Porcentaje (0-100) o monto fijo según el tipo", example = "50.00")
    private BigDecimal valorDescuento;

    @Schema(description = "Primer día de vigencia", example = "2026-12-01")
    private LocalDate fechaInicio;

    @Schema(description = "Último día de vigencia (inclusive)", example = "2026-12-31")
    private LocalDate fechaFin;

    @Schema(description = "Habilitar o deshabilitar la promoción", example = "false")
    private Boolean activo;
}
