package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.reporte.DashboardResumenDto;
import com.example.backendgymteo1.dto.reporte.MetricaAsistenciaDto;
import com.example.backendgymteo1.dto.reporte.ReporteIngresosDto;
import com.example.backendgymteo1.dto.reporte.ReporteMiembrosEstadoDto;
import com.example.backendgymteo1.dto.reporte.ReporteRenovacionesDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reportes y Métricas", description = "Endpoints de reportes financieros, de membresías y métricas de asistencia según rol")
@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;

    @Operation(summary = "Reporte de ingresos por mes y método de pago", description = "Requiere rol ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte de ingresos generado con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReporteIngresosDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere ADMIN)", content = @Content)
    })
    @GetMapping("/ingresos")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ReporteIngresosDto> getReporteIngresos(
            @Parameter(description = "Año a consultar (opcional, histórico si se omite)")
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(reporteService.getReporteIngresos(anio));
    }

    @Operation(summary = "Reporte de miembros activos vs morosos con información detallada", description = "Requiere rol ADMIN o RECEPCIONISTA. Permite filtrar por estado (ACTIVO/MOROSO) y sucursal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estadística y listado de miembros activos vs morosos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReporteMiembrosEstadoDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/miembros-estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ReporteMiembrosEstadoDto> getReporteMiembrosEstado(
            @Parameter(description = "Filtro opcional por estado: ACTIVO o MOROSO")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Filtro opcional por ID de sucursal")
            @RequestParam(required = false) Integer sucursalId) {
        return ResponseEntity.ok(reporteService.getReporteMiembrosEstado(estado, sucursalId));
    }

    @Operation(summary = "Reporte de renovaciones de membresías", description = "Requiere rol ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte de renovaciones generado con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReporteRenovacionesDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere ADMIN)", content = @Content)
    })
    @GetMapping("/renovaciones")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ReporteRenovacionesDto> getReporteRenovaciones(
            @Parameter(description = "Año a consultar (opcional)")
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(reporteService.getReporteRenovaciones(anio));
    }

    @Operation(summary = "Resumen ejecutivo tipo Dashboard", description = "Requiere rol ADMIN o RECEPCIONISTA")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Indicadores principales consolidados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResumenDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<DashboardResumenDto> getDashboardResumen() {
        return ResponseEntity.ok(reporteService.getDashboardResumen());
    }

    @Operation(summary = "Métricas de asistencia según rol del usuario", description = "Retorna métricas de asistencia adaptadas según el rol del usuario autenticado (ADMIN, RECEPCIONISTA o CLIENTE)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas de asistencia obtenidas con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricaAsistenciaDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/asistencias")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'CLIENTE')")
    public ResponseEntity<MetricaAsistenciaDto> getMetricasAsistencia(
            @Parameter(description = "Mes a evaluar (1-12, opcional)")
            @RequestParam(required = false) Integer mes,
            @Parameter(description = "Año a evaluar (opcional)")
            @RequestParam(required = false) Integer anio,
            @Parameter(description = "ID de sucursal a filtrar (opcional)")
            @RequestParam(required = false) Integer sucursalId,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(reporteService.getMetricasAsistencia(currentUser, mes, anio, sucursalId));
    }
}
