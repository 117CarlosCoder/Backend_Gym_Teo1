package com.example.backendgymteo1.dto.socio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Datos para registrar un nuevo socio en el gimnasio")
public class CreateSocioDto {

    @Schema(description = "Número de DPI del socio", example = "1000000000004", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El DPI es obligatorio")
    @Size(min = 13, max = 20, message = "El DPI debe tener entre 13 y 20 caracteres")
    private String dpi;

    @Schema(description = "Nombres del socio", example = "Carlos Raúl", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @Schema(description = "Apellidos del socio", example = "López", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @Schema(description = "Número de teléfono de contacto", example = "55550004")
    private String telefono;

    @Schema(description = "Dirección de residencia del socio", example = "12 Calle 5-45 Zona 1")
    private String direccion;

    @Schema(description = "Fecha de nacimiento del socio", example = "1998-07-20")
    private LocalDate fechaNacimiento;

    @Schema(description = "Correo electrónico único", example = "carlos.socio@gymdemo.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @Schema(description = "ID de la sucursal de registro (por defecto 1 = Sucursal Central)", example = "1")
    private Integer sucursalId;

    @Schema(description = "Fecha de registro del socio", example = "2026-03-01")
    private LocalDate fechaRegistro;

    @Schema(description = "Estado inicial del socio: ACTIVO (por defecto) o INACTIVO", example = "ACTIVO", allowableValues = {"ACTIVO", "INACTIVO"})
    private String estado;
}
