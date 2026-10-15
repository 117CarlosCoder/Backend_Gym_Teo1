package com.example.backendgymteo1.dto.promocion;

import com.example.backendgymteo1.entity.Promocion;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO de respuesta con los datos de una promoción")
public class PromocionResponseDto {

    @Schema(description = "ID de la promoción", example = "1")
    private Integer id;

    @Schema(description = "Nombre de la promoción", example = "Promo Fin de Año")
    private String nombre;

    @Schema(description = "Descripción", example = "10% de descuento en cualquier plan")
    private String descripcion;

    @Schema(description = "Tipo de descuento", example = "PORCENTAJE")
    private Promocion.TipoDescuento tipoDescuento;

    @Schema(description = "Valor del descuento (porcentaje o monto fijo)", example = "10.00")
    private BigDecimal valorDescuento;

    @Schema(description = "Primer día de vigencia", example = "2026-12-01")
    private LocalDate fechaInicio;

    @Schema(description = "Último día de vigencia", example = "2026-12-31")
    private LocalDate fechaFin;

    @Schema(description = "Si está habilitada", example = "true")
    private boolean activo;

    @Schema(description = "Si está vigente hoy (activa y dentro del rango de fechas)", example = "true")
    private boolean vigente;
}
