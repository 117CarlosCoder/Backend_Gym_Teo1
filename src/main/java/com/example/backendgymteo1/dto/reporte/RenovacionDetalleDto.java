package com.example.backendgymteo1.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle de membresía emitida o renovada en el período")
public class RenovacionDetalleDto {

    @Schema(description = "ID de la membresía", example = "58")
    private Integer membresiaId;

    @Schema(description = "ID del socio", example = "10")
    private Integer socioId;

    @Schema(description = "Nombre completo del socio", example = "Carlos López")
    private String nombreSocio;

    @Schema(description = "DPI del socio", example = "2987123450101")
    private String dpi;

    @Schema(description = "Correo del socio", example = "carlos@gym.com")
    private String correo;

    @Schema(description = "Plan contratado", example = "Plan Trimestral")
    private String planNombre;

    @Schema(description = "Tipo de emisión: RENOVACIÓN o NUEVA", example = "RENOVACIÓN")
    private String tipo;

    @Schema(description = "Fecha de inicio", example = "2026-10-01")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha de vencimiento", example = "2026-12-31")
    private LocalDate fechaVencimiento;
}
