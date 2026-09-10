package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.socio.CreateSocioDto;
import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.dto.socio.UpdateSocioDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.SocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Socios", description = "Operaciones de administración y consulta de socios del gimnasio")
@RestController
@RequestMapping("/socios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SocioController {

    private final SocioService socioService;

    @Operation(summary = "Registrar un nuevo socio", description = "Requiere rol ADMIN o RECEPCIONISTA. Recibe datos personales, dirección, fecha de nacimiento, sucursal y estado opcional (ACTIVO/INACTIVO). La contraseña es autogenerada y enviada por correo. Registra evento en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Socio registrado exitosamente con su ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocioResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o correo/DPI duplicado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<SocioResponseDto> create(
            @Valid @RequestBody CreateSocioDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(socioService.create(request, currentUser));
    }

    @Operation(summary = "Listar socios con filtro por estado y paginación", description = "Requiere rol ADMIN, RECEPCIONISTA o ENTRENADOR. Soporta filtrado por estado: ACTIVO (membresía vigente), MOROSO (membresía vencida o sin membresía), INACTIVO (dado de baja) o TODOS. Parámetros: ?estado=ACTIVO&page=0&size=10&sort=id,asc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de socios obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'ENTRENADOR')")
    public ResponseEntity<Page<SocioResponseDto>> findAll(
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(socioService.findAll(estado, pageable));
    }

    @Operation(summary = "Obtener perfil completo del socio autenticado actual (/me)", description = "Extrae los datos personales, membresía actual y última asistencia a partir del token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil completo del socio autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocioResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "El usuario autenticado no tiene perfil de socio", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<SocioResponseDto> findMe(@CurrentUser User currentUser) {
        return ResponseEntity.ok(socioService.findById(currentUser.getId()));
    }

    @Operation(summary = "Consultar datos completos del socio por ID", description = "Retorna todos los datos del socio: datos personales, membresía actual (si existe), última asistencia y estado. Valida que el socio exista (404 si no existe). Requiere rol ADMIN, RECEPCIONISTA o ENTRENADOR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos completos del socio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocioResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'ENTRENADOR')")
    public ResponseEntity<SocioResponseDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(socioService.findById(id));
    }

    @Operation(summary = "Actualizar información permitida de un socio", description = "Permite edición de: nombre, email, teléfono, dirección, fecha_nacimiento y sucursal. Valida email único si cambia. NO permite editar ID ni fecha de creación. Registra en auditoría quién realizó el cambio y qué campos fueron modificados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Socio actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocioResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o email ya en uso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<SocioResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSocioDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(socioService.update(id, request, currentUser));
    }

    @Operation(summary = "Desactivar un socio por ID (Soft Delete)", description = "Requiere rol ADMIN o RECEPCIONISTA. Registra en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Socio desactivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Void> remove(
            @PathVariable Integer id,
            @CurrentUser User currentUser) {
        socioService.remove(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reactivar un socio dado de baja", description = "Restaura la cuenta de un socio inactivo a activo. Requiere rol ADMIN o RECEPCIONISTA. Registra en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Socio reactivado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocioResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<SocioResponseDto> reactivar(
            @PathVariable Integer id,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(socioService.reactivar(id, currentUser));
    }
}
