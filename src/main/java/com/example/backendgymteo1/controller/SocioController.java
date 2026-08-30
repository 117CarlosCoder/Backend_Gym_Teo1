package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.dto.socio.CreateSocioDto;
import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.dto.socio.UpdateSocioDto;
import com.example.backendgymteo1.service.SocioService;
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

@Tag(name = "Socios", description = "Operaciones de administración y consulta de socios del gimnasio")
@RestController
@RequestMapping("/socios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SocioController {

    private final SocioService socioService;

    @Operation(summary = "Registrar un nuevo socio", description = "Requiere rol ADMIN o RECEPCIONISTA. La contraseña es autogenerada de forma segura y notificada al socio por correo electrónico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Socio registrado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocioResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<SocioResponseDto> create(@Valid @RequestBody CreateSocioDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(socioService.create(request));
    }

    @Operation(summary = "Listar todos los socios activos", description = "Requiere rol ADMIN, RECEPCIONISTA o ENTRENADOR")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de socios obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'ENTRENADOR')")
    public ResponseEntity<List<SocioResponseDto>> findAll() {
        return ResponseEntity.ok(socioService.findAll());
    }

    @Operation(summary = "Buscar socio por ID", description = "Requiere rol ADMIN, RECEPCIONISTA o ENTRENADOR")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Socio encontrado",
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

    @Operation(summary = "Actualizar información de un socio por ID", description = "Requiere rol ADMIN o RECEPCIONISTA")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Socio actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocioResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<SocioResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSocioDto request) {
        return ResponseEntity.ok(socioService.update(id, request));
    }

    @Operation(summary = "Desactivar un socio por ID", description = "Requiere rol ADMIN o RECEPCIONISTA")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Socio desactivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Void> remove(@PathVariable Integer id) {
        socioService.remove(id);
        return ResponseEntity.noContent().build();
    }
}
