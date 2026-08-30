package com.example.backendgymteo1.dto.user;

import com.example.backendgymteo1.entity.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para crear un nuevo usuario / miembro del gimnasio desde la administración (la contraseña se autogenera)")
public class CreateUserDto {

    @Schema(description = "Número de DPI del usuario", example = "1000000000088", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El DPI es obligatorio")
    @Size(min = 13, max = 20, message = "El DPI debe tener entre 13 y 20 caracteres")
    private String dpi;

    @Schema(description = "Nombres del usuario", example = "Rodrigo Fernando", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @Schema(description = "Apellidos del usuario", example = "Alvarado Soto", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @Schema(description = "Número de teléfono de contacto", example = "55550088")
    private String telefono;

    @Schema(description = "Correo electrónico único", example = "rodrigo.coach@gymdemo.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @Schema(description = "Rol asignado al usuario", example = "ENTRENADOR", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
}
