package com.example.backendgymteo1.dto.auth;

import com.example.backendgymteo1.entity.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para el registro de un nuevo usuario")
public class RegisterRequestDto {

    @Schema(description = "Número de DPI del usuario", example = "1000000000099", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El DPI es obligatorio")
    @Size(min = 13, max = 20, message = "El DPI debe tener entre 13 y 20 caracteres")
    private String dpi;

    @Schema(description = "Nombres del usuario", example = "Mateo Sebastián", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @Schema(description = "Apellidos del usuario", example = "Morales Castillo", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @Schema(description = "Número de teléfono de contacto", example = "55550099")
    private String telefono;

    @Schema(description = "Correo electrónico único", example = "mateo.morales@gymdemo.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @Schema(description = "Contraseña de acceso (mínimo 6 caracteres)", example = "Client123*", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String contrasenia;

    @Schema(description = "Rol asignado en el sistema", example = "CLIENTE")
    private Rol rol;
}
