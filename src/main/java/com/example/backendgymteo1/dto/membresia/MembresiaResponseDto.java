package com.example.backendgymteo1.dto.membresia;

import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos de respuesta de una membresía de socio")
public class MembresiaResponseDto {

    @Schema(description = "Identificador único de la membresía", example = "1")
    private Integer id;

    @Schema(description = "Datos del socio asociado")
    private SocioResponseDto socio;

    @Schema(description = "Plan de membresía contratado")
    private PlanMembresiaResponseDto plan;

    @Schema(description = "Estado actual de la membresía")
    private EstadoMembresiaResponseDto estado;

    @Schema(description = "Fecha de inicio de la vigencia", example = "2026-03-01")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha de vencimiento calculada", example = "2026-03-31")
    private LocalDate fechaVencimiento;

    @Schema(description = "Indica si la membresía se encuentra activa actualmente", example = "true")
    private boolean activa;

    @Schema(description = "Días restantes de vigencia a partir de hoy", example = "27")
    private Long diasRestantes;
}
