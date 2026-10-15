package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto;
import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto.Agrupacion;
import com.example.backendgymteo1.service.MetricasAsistenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Asistencias", description = "Operaciones de control y registro de asistencia al gimnasio")
@RestController
@RequestMapping("/asistencias/metricas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MetricasAsistenciaController {

    private final MetricasAsistenciaService metricasAsistenciaService;

    @Operation(
            summary = "Métricas de asistencia",
            description = "Total de asistencias, socios únicos, promedio diario, serie temporal agrupada por DIA, SEMANA (ISO) o MES y desglose por sucursal en un rango de fechas (máximo 366 días). Parámetro idSucursal opcional. Requiere rol ADMIN o RECEPCIONISTA"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas calculadas exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricasAsistenciaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido o mayor a 366 días", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN o RECEPCIONISTA)", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<MetricasAsistenciaResponseDto> obtenerMetricas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "DIA") Agrupacion agrupacion,
            @RequestParam(required = false) Integer idSucursal) {
        return ResponseEntity.ok(metricasAsistenciaService.obtenerMetricas(desde, hasta, agrupacion, idSucursal));
    }
}
