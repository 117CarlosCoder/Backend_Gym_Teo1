package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.asistencia.AsistenciaDetalleDto;
import com.example.backendgymteo1.dto.asistencia.RegistrarAsistenciaDto;
import com.example.backendgymteo1.dto.asistencia.RegistroAsistenciaResponseDto;
import com.example.backendgymteo1.entity.Asistencia;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.AsistenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Asistencias", description = "Operaciones de control y registro de asistencia al gimnasio")
@RestController
@RequestMapping("/asistencias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    @Operation(
            summary = "Registrar entrada de un socio al gimnasio",
            description = "Recibe id_socio y sucursal_id. Valida que el socio existe, que su membresía está activa y que la sucursal es válida. Registra la hora de entrada y el recepcionista autenticado. Retorna confirmación con los datos completos del socio. Requiere rol RECEPCIONISTA"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Asistencia registrada exitosamente con confirmación y datos del socio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistroAsistenciaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Membresía no activa, sucursal no activa o datos inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Socio o sucursal no encontrada", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN o RECEPCIONISTA)", content = @Content)
    })
    @PostMapping({"/registro"})
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<RegistroAsistenciaResponseDto> registrarEntrada(
            @Valid @RequestBody RegistrarAsistenciaDto request,
            @CurrentUser User currentUser) {
        RegistroAsistenciaResponseDto response = asistenciaService.registrarEntrada(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Registrar salida de un socio",
            description = "Marca la hora de salida de una asistencia abierta (socio dentro del gimnasio). "
                    + "Rechaza la operación si la salida ya fue registrada. Requiere rol ADMIN o RECEPCIONISTA."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Salida registrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AsistenciaDetalleDto.class))),
            @ApiResponse(responseCode = "400", description = "La salida ya había sido registrada", content = @Content),
            @ApiResponse(responseCode = "404", description = "Asistencia no encontrada", content = @Content)
    })
    @PostMapping("/{id}/salida")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<AsistenciaDetalleDto> registrarSalida(
            @PathVariable Integer id,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(asistenciaService.registrarSalida(id, currentUser));
    }

    @Operation(
            summary = "Asistencias del día",
            description = "Lista las entradas registradas en la fecha indicada (por defecto hoy) con su hora de entrada, "
                    + "hora de salida y si el socio sigue dentro. Filtro opcional por sucursal. Requiere rol ADMIN o RECEPCIONISTA."
    )
    @GetMapping("/hoy")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<AsistenciaDetalleDto>> asistenciasDelDia(
            @Parameter(description = "Fecha a consultar (YYYY-MM-DD). Por defecto, hoy.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(description = "ID de la sucursal (opcional)")
            @RequestParam(required = false) Integer sucursalId) {
        return ResponseEntity.ok(asistenciaService.listarAsistenciasDelDia(fecha, sucursalId));
    }

    @Operation(
            summary = "Mis asistencias",
            description = "Historial de entradas y salidas del socio autenticado, de la más reciente a la más antigua. Requiere rol CLIENTE."
    )
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CLIENTE')")
    public ResponseEntity<List<AsistenciaDetalleDto>> misAsistencias(
            @Parameter(description = "Cantidad máxima de registros (por defecto 30)")
            @RequestParam(defaultValue = "30") int limite,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(asistenciaService.misAsistencias(currentUser, limite));
    }

    @Operation(summary = "Consultar asistencia por ID", description = "Requiere rol ADMIN o RECEPCIONISTA")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Asistencia> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(asistenciaService.findById(id));
    }

    @Operation(summary = "Consultar historial de asistencias de un socio", description = "Requiere rol ADMIN, RECEPCIONISTA o ENTRENADOR")
    @GetMapping("/socio/{socioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'ENTRENADOR')")
    public ResponseEntity<List<Asistencia>> findBySocioId(@PathVariable Integer socioId) {
        return ResponseEntity.ok(asistenciaService.findBySocioId(socioId));
    }
}
