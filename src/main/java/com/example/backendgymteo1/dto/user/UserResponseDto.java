package com.example.backendgymteo1.dto.user;

import com.example.backendgymteo1.entity.Rol;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos de respuesta con información del usuario")
public class UserResponseDto {

    @Schema(description = "Identificador único del usuario", example = "1")
    private Integer id;

    @Schema(description = "Número de DPI del usuario", example = "1234567890101")
    private String dpi;

    @Schema(description = "Nombres del usuario", example = "Juan Carlos")
    private String nombres;

    @Schema(description = "Apellidos del usuario", example = "Pérez López")
    private String apellidos;

    @Schema(description = "Número de teléfono", example = "+502 55551234")
    private String telefono;

    @Schema(description = "Dirección de residencia", example = "10 Calle 4-20 Zona 1")
    private String direccion;

    @Schema(description = "Fecha de nacimiento", example = "1995-05-15")
    private java.time.LocalDate fechaNacimiento;

    @Schema(description = "Correo electrónico", example = "juan.perez@gym.com")
    private String correo;

    @Schema(description = "Rol del usuario en el gimnasio", example = "CLIENTE")
    private Rol rol;

    @Schema(description = "Estado de la cuenta (activo o inactivo)", example = "true")
    private Boolean estado;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Contraseña temporal autogenerada (solo se incluye en la respuesta al crear el usuario)", example = "aK8xP2wM4Q")
    private String contraseniaTemporal;

    @Schema(description = "Fecha y hora de creación de la cuenta", example = "2026-08-26T10:15:30")
    private LocalDateTime creadoEn;

    @Schema(description = "Fecha y hora de última actualización", example = "2026-08-26T12:30:00")
    private LocalDateTime actualizadoEn;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Fecha y hora de desactivación (si aplica)", example = "2026-08-27T10:45:00")
    private LocalDateTime eliminadoEn;
}
