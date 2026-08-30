package com.example.backendgymteo1.dto.socio;

import com.example.backendgymteo1.dto.user.UserResponseDto;
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
@Schema(description = "Datos de respuesta con información del socio")
public class SocioResponseDto {

    @Schema(description = "Identificador único del socio", example = "4")
    private Integer id;

    @Schema(description = "Fecha de registro del socio en el gimnasio", example = "2026-03-01")
    private LocalDate fechaRegistro;

    @Schema(description = "Datos del usuario asociado al socio")
    private UserResponseDto usuario;
}
