package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.configuracion.RecargoTardioResponseDto;
import com.example.backendgymteo1.dto.configuracion.ReglasAbonoDto;
import com.example.backendgymteo1.dto.configuracion.ReglasCongelamientoDto;
import com.example.backendgymteo1.dto.configuracion.UpdateRecargoTardioDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.ConfiguracionSistemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Configuración", description = "Parámetros del sistema modificables por el administrador")
@RestController
@RequestMapping("/configuracion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConfiguracionController {

    private final ConfiguracionSistemaService configuracionService;

    @Operation(summary = "Consultar el recargo por pago tardío", description = "Retorna el porcentaje aplicado sobre el precio del plan cuando la membresía ya venció al pagar. Requiere rol ADMIN o RECEPCIONISTA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuración vigente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecargoTardioResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN o RECEPCIONISTA)", content = @Content)
    })
    @GetMapping("/recargo-tardio")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<RecargoTardioResponseDto> consultarRecargoTardio() {
        return ResponseEntity.ok(configuracionService.consultarRecargoTardio());
    }

    @Operation(summary = "Modificar el recargo por pago tardío", description = "Define el porcentaje (0-100) del recargo. Aplica a las cotizaciones y pagos nuevos; las facturas ya emitidas no cambian. Requiere rol ADMIN. Registra evento en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recargo actualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecargoTardioResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Porcentaje fuera del rango 0-100", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content)
    })
    @PutMapping("/recargo-tardio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecargoTardioResponseDto> actualizarRecargoTardio(
            @Valid @RequestBody UpdateRecargoTardioDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(configuracionService.actualizarRecargoTardio(request.getPorcentaje(), currentUser));
    }

    @Operation(summary = "Consultar las reglas de congelamiento", description = "Días mínimos y máximos por congelamiento y cantidad permitida por año. Requiere rol ADMIN o RECEPCIONISTA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reglas vigentes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReglasCongelamientoDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/congelamiento")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ReglasCongelamientoDto> consultarReglasCongelamiento() {
        return ResponseEntity.ok(configuracionService.consultarReglasCongelamiento());
    }

    @Operation(summary = "Modificar las reglas de congelamiento", description = "Aplica a los congelamientos nuevos. Requiere rol ADMIN. Registra evento en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reglas actualizadas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReglasCongelamientoDto.class))),
            @ApiResponse(responseCode = "400", description = "Valores fuera de rango o mínimo mayor que máximo", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content)
    })
    @PutMapping("/congelamiento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReglasCongelamientoDto> actualizarReglasCongelamiento(
            @Valid @RequestBody ReglasCongelamientoDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(configuracionService.actualizarReglasCongelamiento(request, currentUser));
    }

    @Operation(summary = "Consultar las reglas de pagos en abonos", description = "Porcentaje mínimo del primer abono y plazo para liquidar el saldo. Requiere rol ADMIN o RECEPCIONISTA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reglas vigentes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReglasAbonoDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/abonos")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ReglasAbonoDto> consultarReglasAbono() {
        return ResponseEntity.ok(configuracionService.consultarReglasAbono());
    }

    @Operation(summary = "Modificar las reglas de pagos en abonos", description = "Aplica a los pagos nuevos. Requiere rol ADMIN. Registra evento en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reglas actualizadas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReglasAbonoDto.class))),
            @ApiResponse(responseCode = "400", description = "Valores fuera de rango", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content)
    })
    @PutMapping("/abonos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReglasAbonoDto> actualizarReglasAbono(
            @Valid @RequestBody ReglasAbonoDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(configuracionService.actualizarReglasAbono(request, currentUser));
    }
}
