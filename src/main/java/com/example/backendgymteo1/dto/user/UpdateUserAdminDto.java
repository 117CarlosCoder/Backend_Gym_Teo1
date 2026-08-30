package com.example.backendgymteo1.dto.user;

import com.example.backendgymteo1.entity.Rol;
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
@Schema(description = "Datos para que el Administrador actualice información de un usuario")
public class UpdateUserAdminDto {

    @Schema(description = "Número de DPI del usuario", example = "1000000000002")
    @Size(min = 13, max = 20, message = "El DPI debe tener entre 13 y 20 caracteres")
    private String dpi;

    @Schema(description = "Nombres del usuario", example = "Carlos Raúl")
    private String nombres;

    @Schema(description = "Apellidos del usuario", example = "López (Recepción Modificado)")
    private String apellidos;

    @Schema(description = "Número de teléfono de contacto", example = "55550002")
    private String telefono;

    @Schema(description = "Correo electrónico único", example = "calin10@outlook.es")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @Schema(description = "Nueva contraseña (si se envía, se actualizará encriptada)", example = "RecepNueva123*")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String contrasenia;

    @Schema(description = "Rol asignado al usuario", example = "RECEPCIONISTA")
    private Rol rol;

    @Schema(description = "Estado de la cuenta (activo o inactivo)", example = "true")
    private Boolean estado;
}
