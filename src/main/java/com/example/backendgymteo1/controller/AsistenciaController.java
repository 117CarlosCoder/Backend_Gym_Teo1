package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.asistencia.RegistrarAsistenciaDto;
import com.example.backendgymteo1.dto.asistencia.RegistroAsistenciaResponseDto;
import com.example.backendgymteo1.entity.Asistencia;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.AsistenciaService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PreAuthorize("hasAnyRole('RECEPCIONISTA')")
    public ResponseEntity<RegistroAsistenciaResponseDto> registrarEntrada(
            @Valid @RequestBody RegistrarAsistenciaDto request,
            @CurrentUser User currentUser) {
        RegistroAsistenciaResponseDto response = asistenciaService.registrarEntrada(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
