package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.user.CreateUserDto;
import com.example.backendgymteo1.dto.user.UpdateProfileDto;
import com.example.backendgymteo1.dto.user.UpdateUserAdminDto;
import com.example.backendgymteo1.dto.user.UserResponseDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.mapper.UserMapper;
import com.example.backendgymteo1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Usuarios", description = "Operaciones de administración y consulta de usuarios del gimnasio")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Crear un nuevo usuario / entrenador / miembro", description = "Requiere rol ADMIN. La contraseña es autogenerada de forma segura y notificada al usuario por correo electrónico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN)", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody CreateUserDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @Operation(summary = "Listar todos los usuarios activos", description = "Requiere rol ADMIN o RECEPCIONISTA")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado (Token ausente o inválido)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Rol insuficiente)", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<UserResponseDto>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Buscar usuario por ID", description = "Requiere rol ADMIN o RECEPCIONISTA")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<UserResponseDto> findOne(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "Obtener el perfil del usuario autenticado actual", description = "Extrae los datos a partir del token JWT de la sesión")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil del usuario autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> findOneMe(@CurrentUser User currentUser) {
        return ResponseEntity.ok(userMapper.toDto(currentUser));
    }

    @Operation(summary = "Actualizar perfil del usuario autenticado actual", description = "Permite modificar datos de contacto y/o cambiar contraseña")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o contraseña actual incorrecta", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateProfile(
            @CurrentUser User currentUser,
            @Valid @RequestBody UpdateProfileDto request) {
        return ResponseEntity.ok(userService.updateProfile(currentUser.getId(), request));
    }

    @Operation(summary = "Actualizar información de un usuario por ID", description = "Requiere rol ADMIN. Permite modificar datos, rol, estado y restablecer contraseña")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN)", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateByAdmin(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserAdminDto request) {
        return ResponseEntity.ok(userService.updateByAdmin(id, request));
    }

    @Operation(summary = "Desactivar un usuario por ID", description = "Requiere rol ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario desactivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> remove(@PathVariable Integer id) {
        userService.remove(id);
        return ResponseEntity.noContent().build();
    }
}
