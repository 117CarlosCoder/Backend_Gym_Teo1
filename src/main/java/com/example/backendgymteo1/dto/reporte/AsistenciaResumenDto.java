package com.example.backendgymteo1.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle resumido de un registro de asistencia")
public class AsistenciaResumenDto {

    @Schema(description = "ID del registro de asistencia", example = "101")
    private Integer id;

    @Schema(description = "ID del socio", example = "3")
    private Integer socioId;

    @Schema(description = "Nombre completo del socio", example = "Lucia Hernandez")
    private String nombreSocio;

    @Schema(description = "DPI del socio", example = "2987123450101")
    private String socioDpi;

    @Schema(description = "Correo del socio", example = "lucia@gym.com")
    private String correoSocio;

    @Schema(description = "Plan de membresía activo del socio", example = "Plan Anual VIP")
    private String planNombre;

    @Schema(description = "Nombre de la sucursal", example = "Sede Norte")
    private String nombreSucursal;

    @Schema(description = "Fecha de asistencia", example = "2026-10-15")
    private LocalDate fecha;

    @Schema(description = "Hora de ingreso", example = "07:30:00")
    private LocalTime horaEntrada;

    @Schema(description = "Hora de salida si fue registrada", example = "09:00:00")
    private LocalTime horaSalida;
}
