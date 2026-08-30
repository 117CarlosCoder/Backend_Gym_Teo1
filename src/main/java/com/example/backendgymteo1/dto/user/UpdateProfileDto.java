package com.example.backendgymteo1.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para que el propio usuario autenticado actualice su perfil")
public class UpdateProfileDto {

    @Schema(description = "Nombres del usuario", example = "Carlos Raúl")
    private String nombres;

    @Schema(description = "Apellidos del usuario", example = "López Actualizado")
    private String apellidos;

    @Schema(description = "Número de teléfono de contacto", example = "55550001")
    private String telefono;

    @Schema(description = "Correo electrónico del usuario", example = "clp64413@gmail.com")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @Schema(description = "Contraseña actual (requerida únicamente si desea cambiar la contraseña)", example = "Admin123*")
    private String contraseniaActual;

    @Schema(description = "Nueva contraseña deseada (mínimo 6 caracteres)", example = "NuevoAdmin123*")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String nuevaContrasenia;
}
