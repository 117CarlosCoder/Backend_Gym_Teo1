package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.membresia.CancelarMembresiaRequestDto;
import com.example.backendgymteo1.dto.membresia.CreateMembresiaDto;
import com.example.backendgymteo1.dto.membresia.MembresiaResponseDto;
import com.example.backendgymteo1.dto.membresia.UpdateMembresiaDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Membresías", description = "Operaciones para la emisión, consulta y gestión de membresías multisucursal")
@RestController
@RequestMapping("/membresias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MembresiaController {

    private final MembresiaService membresiaService;

    @Operation(summary = "Crear / Registrar una nueva membresía", description = "Requiere rol ADMIN o RECEPCIONISTA. Recibe socio_id, tipo_membresia_id, sucursal_ids (array para multisucursal) y fecha_inicio. Calcula automáticamente la fecha de vencimiento según la duración del tipo y registra auditoría.")
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
    public ResponseEntity<MembresiaResponseDto> create(
            @Valid @RequestBody CreateMembresiaDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membresiaService.create(request, currentUser));
    }

    @Operation(summary = "Listar membresías con filtros avanzados y paginación", description = "Requiere rol ADMIN, RECEPCIONISTA o ENTRENADOR. Filtros disponibles: socio, tipo/plan, sucursal, estado (activa/cancelada/vencida) y fechas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de membresías obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'ENTRENADOR')")
    public ResponseEntity<Page<MembresiaResponseDto>> findAll(
            @RequestParam(required = false) Integer socioId,
            @RequestParam(required = false) Integer planId,
            @RequestParam(required = false) Integer tipoMembresiaId,
            @RequestParam(required = false) Integer sucursalId,
            @RequestParam(required = false) Integer estadoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        Integer effectivePlanId = planId != null ? planId : tipoMembresiaId;
        return ResponseEntity.ok(membresiaService.findAllPaged(
                socioId, effectivePlanId, sucursalId, estadoId, estado, fechaDesde, fechaHasta, pageable));
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

    @Operation(summary = "Editar membresía por ID", description = "Permite editar: tipo_membresia_id, sucursal_ids (agregar/quitar) y fecha_vencimiento. Aplica validaciones lógicas. NO permite editar ID, socio ni fecha de creación. Registra evento en auditoría. Requiere ADMIN o RECEPCIONISTA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membresía actualizada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MembresiaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validaciones de lógica inválidas (ej. fecha vencimiento anterior a inicio)", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Membresía, plan o sucursal no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<MembresiaResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateMembresiaDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(membresiaService.update(id, request, currentUser));
    }

    @Operation(summary = "Cancelar formalmente una membresía por ID", description = "Requiere rol ADMIN o RECEPCIONISTA. Recibe en body: motivo (dropdown/texto), comentarios, reembolso (true/false) y monto_reembolso (si aplica). Cambia estado a 'CANCELADA', registra fecha_cancelacion, usuario que canceló, auditoría y envía email al socio.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membresía cancelada exitosamente y socio notificado por correo",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MembresiaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Monto de reembolso inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Membresía no encontrada", content = @Content)
    })
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<MembresiaResponseDto> cancelarPost(
            @PathVariable Integer id,
            @Valid @RequestBody CancelarMembresiaRequestDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(membresiaService.cancelar(id, request, currentUser));
    }

    @Operation(summary = "Cancelar membresía vía PATCH (compatibilidad)", description = "Soporta cancelación con body estructurado o parámetro de motivo.")
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<MembresiaResponseDto> cancelarPatch(
            @PathVariable Integer id,
            @RequestBody(required = false) CancelarMembresiaRequestDto request,
            @RequestParam(required = false) String motivo,
            @CurrentUser User currentUser) {
        if (request == null) {
            request = CancelarMembresiaRequestDto.builder()
                    .motivo(motivo != null ? motivo : "Cancelación vía PATCH")
                    .reembolso(false)
                    .build();
        }
        return ResponseEntity.ok(membresiaService.cancelar(id, request, currentUser));
    }
}
