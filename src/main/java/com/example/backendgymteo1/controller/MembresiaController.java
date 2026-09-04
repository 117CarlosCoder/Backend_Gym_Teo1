package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.membresia.CreateMembresiaDto;
import com.example.backendgymteo1.dto.membresia.MembresiaResponseDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.MembresiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Membresías", description = "Operaciones para la emisión, consulta y gestión de membresías de socios")
@RestController
@RequestMapping("/membresias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MembresiaController {

    private final MembresiaService membresiaService;

    @Operation(summary = "Crear / Registrar una nueva membresía", description = "Requiere rol ADMIN o RECEPCIONISTA. La fecha de vencimiento se calcula automáticamente según la duración del plan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Membresía creada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MembresiaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o socio con membresía activa vigente", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN o RECEPCIONISTA)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Socio o Plan no encontrado", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<MembresiaResponseDto> create(@Valid @RequestBody CreateMembresiaDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membresiaService.create(request));
    }

    @Operation(summary = "Listar membresías con filtros opcionales", description = "Requiere rol ADMIN, RECEPCIONISTA o ENTRENADOR. Permite filtrar por socio, plan, estado y rango de fechas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de membresías obtenida exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MembresiaResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'ENTRENADOR')")
    public ResponseEntity<List<MembresiaResponseDto>> findAll(
            @RequestParam(required = false) Integer socioId,
            @RequestParam(required = false) Integer planId,
            @RequestParam(required = false) Integer estadoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        return ResponseEntity.ok(membresiaService.findAll(socioId, planId, estadoId, fechaDesde, fechaHasta));
    }

    @Operation(summary = "Consultar membresías del socio autenticado en sesión (/me)", description = "Retorna el historial completo de membresías del usuario socio autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de membresías obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "El usuario autenticado no tiene perfil de socio", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<List<MembresiaResponseDto>> findMyMembresias(@CurrentUser User currentUser) {
        return ResponseEntity.ok(membresiaService.findBySocioId(currentUser.getId()));
    }

    @Operation(summary = "Consultar membresía activa del socio autenticado en sesión (/me/activa)", description = "Retorna la membresía vigente actual del socio que inició sesión")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membresía activa vigente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MembresiaResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "No tiene ninguna membresía activa vigente", content = @Content)
    })
    @GetMapping("/me/activa")
    public ResponseEntity<MembresiaResponseDto> findMyActiveMembresia(@CurrentUser User currentUser) {
        return ResponseEntity.ok(membresiaService.findActiveBySocioId(currentUser.getId()));
    }

    @Operation(summary = "Buscar membresía por ID", description = "Requiere rol ADMIN, RECEPCIONISTA o ENTRENADOR")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membresía encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MembresiaResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Membresía no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'ENTRENADOR')")
    public ResponseEntity<MembresiaResponseDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(membresiaService.findById(id));
    }

    @Operation(summary = "Consultar membresías de un socio por su ID de socio", description = "Requiere rol ADMIN, RECEPCIONISTA o ENTRENADOR")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de membresías del socio",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MembresiaResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado", content = @Content)
    })
    @GetMapping("/socio/{socioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'ENTRENADOR')")
    public ResponseEntity<List<MembresiaResponseDto>> findBySocioId(@PathVariable Integer socioId) {
        return ResponseEntity.ok(membresiaService.findBySocioId(socioId));
    }

    @Operation(summary = "Cancelar una membresía por ID", description = "Requiere rol ADMIN o RECEPCIONISTA")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membresía cancelada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MembresiaResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Membresía no encontrada", content = @Content)
    })
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<MembresiaResponseDto> cancelar(
            @PathVariable Integer id,
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(membresiaService.cancelar(id, motivo));
    }
}
