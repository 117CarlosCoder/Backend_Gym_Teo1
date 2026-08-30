package com.example.backendgymteo1.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para el inicio de sesión")
public class LoginRequestDto {

    @Schema(description = "Correo electrónico del usuario", example = "clp64413@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @Schema(description = "Contraseña del usuario", example = "Admin123*", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenia;
}
