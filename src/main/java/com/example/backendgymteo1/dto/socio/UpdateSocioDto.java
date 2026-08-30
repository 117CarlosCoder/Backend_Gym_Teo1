package com.example.backendgymteo1.dto.socio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para actualizar la información de un socio")
public class UpdateSocioDto {

    @Schema(description = "Número de DPI del socio", example = "1000000000004")
    @Size(min = 13, max = 20, message = "El DPI debe tener entre 13 y 20 caracteres")
    private String dpi;

    @Schema(description = "Nombres del socio", example = "Carlos Raúl")
    private String nombres;

    @Schema(description = "Apellidos del socio", example = "López Modificado")
    private String apellidos;

    @Schema(description = "Número de teléfono de contacto", example = "55550004")
    private String telefono;

    @Schema(description = "Correo electrónico del socio", example = "carlos.modificado@gymdemo.com")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @Schema(description = "Fecha de registro del socio", example = "2026-03-01")
    private LocalDate fechaRegistro;

    @Schema(description = "Estado de la cuenta del socio (activo o inactivo)", example = "true")
    private Boolean estado;
}
