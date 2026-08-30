package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.dto.auth.AuthResponseDto;
import com.example.backendgymteo1.dto.auth.LoginRequestDto;
import com.example.backendgymteo1.dto.auth.RegisterRequestDto;
import com.example.backendgymteo1.dto.common.ApiResponseDto;
import com.example.backendgymteo1.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación", description = "Endpoints para registro, inicio y cierre de sesión en el gimnasio")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario con correo y contraseña, retornando el token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario no autorizado o inactivo", content = @Content)
    })
    @PostMapping("/signin")
    public ResponseEntity<AuthResponseDto> signIn(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Registrar nuevo usuario / miembro", description = "Crea una nueva cuenta de usuario en el gimnasio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (correo/DPI duplicado o campos incorrectos)", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Cerrar sesión", description = "Invalida la sesión actual del cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión cerrada con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/signout")
    public ResponseEntity<ApiResponseDto> signOut() {
        return ResponseEntity.ok(new ApiResponseDto("Sesión cerrada con éxito."));
    }
}
