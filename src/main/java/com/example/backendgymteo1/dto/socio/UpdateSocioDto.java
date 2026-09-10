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
@Schema(description = "Datos permitidos para actualizar la información de un socio")
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

    @Schema(description = "Dirección de residencia", example = "12 Calle 5-45 Zona 1")
    private String direccion;

    @Schema(description = "Fecha de nacimiento", example = "1998-07-20")
    private LocalDate fechaNacimiento;

    @Schema(description = "Correo electrónico del socio", example = "carlos.modificado@gymdemo.com")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @Schema(description = "ID de la sucursal base", example = "1")
    private Integer sucursalId;

    @Schema(description = "Estado de la cuenta del socio (activo o inactivo)", example = "true")
    private Boolean estado;

    @Schema(description = "Estado operativo del socio: ACTIVO o INACTIVO", example = "ACTIVO", allowableValues = {"ACTIVO", "INACTIVO"})
    private String estadoSocio;
}
