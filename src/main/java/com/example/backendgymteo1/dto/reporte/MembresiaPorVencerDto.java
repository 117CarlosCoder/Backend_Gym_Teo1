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
@Schema(description = "Detalle de socio con membresía próxima a vencer")
public class MembresiaPorVencerDto {

    @Schema(description = "ID de la membresía", example = "42")
    private Integer membresiaId;

    @Schema(description = "ID del socio", example = "10")
    private Integer socioId;

    @Schema(description = "Nombre completo del socio", example = "Carlos López")
    private String nombreSocio;

    @Schema(description = "DPI del socio", example = "2987123450101")
    private String dpi;

    @Schema(description = "Correo del socio", example = "carlos@gym.com")
    private String correo;

    @Schema(description = "Teléfono de contacto", example = "+502 5555-1234")
    private String telefono;

    @Schema(description = "Plan contratado", example = "Plan Mensual")
    private String planNombre;

    @Schema(description = "Nombre de sucursal asignada", example = "Central")
    private String sucursalNombre;

    @Schema(description = "Fecha exacta de vencimiento", example = "2026-10-20")
    private LocalDate fechaVencimiento;

    @Schema(description = "Días restantes antes del vencimiento", example = "5")
    private Long diasRestantes;
}
