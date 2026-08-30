package com.example.backendgymteo1.dto.auth;

import com.example.backendgymteo1.entity.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta de autenticación con token JWT")
public class AuthResponseDto {

    @Schema(description = "Token JWT para autorización en cabeceras Bearer", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Correo del usuario autenticado", example = "admin@gym.com")
    private String correo;

    @Schema(description = "Rol del usuario en el gimnasio", example = "ADMIN")
    private Rol rol;
}
