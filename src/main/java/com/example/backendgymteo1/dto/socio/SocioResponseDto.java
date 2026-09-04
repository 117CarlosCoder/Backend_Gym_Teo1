package com.example.backendgymteo1.dto.socio;

import com.example.backendgymteo1.dto.sucursal.SucursalResponseDto;
import com.example.backendgymteo1.dto.user.UserResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@Schema(description = "Datos de respuesta completos con información del socio, membresía actual y última asistencia")
public class SocioResponseDto {

    @Schema(description = "Identificador único del socio", example = "4")
    private Integer id;

    @Schema(description = "Fecha de registro del socio en el gimnasio", example = "2026-03-01")
    private LocalDate fechaRegistro;

    @Schema(description = "Estado actual del socio (activo o inactivo)", example = "true")
    private Boolean estado;

    @Schema(description = "Datos personales del usuario asociado al socio")
    private UserResponseDto usuario;

    @Schema(description = "Sucursal base / asignada al socio")
    private SucursalResponseDto sucursal;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Membresía actual vigente del socio (si existe)")
    private MembresiaResumenDto membresiaActual;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Última asistencia registrada en el gimnasio (si existe)")
    private UltimaAsistenciaDto ultimaAsistencia;
}
