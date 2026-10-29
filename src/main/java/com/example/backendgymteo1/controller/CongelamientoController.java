package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.membresia.CongelamientoResponseDto;
import com.example.backendgymteo1.dto.membresia.CreateCongelamientoDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.CongelamientoService;
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

@Tag(name = "Congelamiento de membresías", description = "Congelar temporalmente una membresía sin consumir su tiempo")
@RestController
@RequestMapping("/membresias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CongelamientoController {

    private final CongelamientoService congelamientoService;

    @Operation(summary = "Congelar una membresía", description = "Valida las reglas del gimnasio (días mínimos y máximos, cantidad por año, sin traslapes, membresía activa y vigente) y suma los días congelados al vencimiento. Requiere rol ADMIN o RECEPCIONISTA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Congelamiento registrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CongelamientoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "No cumple las reglas de congelamiento", content = @Content),
            @ApiResponse(responseCode = "404", description = "Membresía no encontrada", content = @Content)
    })
    @PostMapping("/{idMembresia}/congelamientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<CongelamientoResponseDto> congelar(
            @PathVariable Integer idMembresia,
            @Valid @RequestBody CreateCongelamientoDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(congelamientoService.congelar(idMembresia, request, currentUser));
    }

    @Operation(summary = "Historial de congelamientos de una membresía", description = "Requiere rol ADMIN o RECEPCIONISTA.")
    @GetMapping("/{idMembresia}/congelamientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<CongelamientoResponseDto>> listarPorMembresia(@PathVariable Integer idMembresia) {
        return ResponseEntity.ok(congelamientoService.listarPorMembresia(idMembresia));
    }

    @Operation(summary = "Descongelar / cancelar un congelamiento", description = "Termina el congelamiento hoy y descuenta del vencimiento los días que no se usaron. Si aún no había iniciado, se anula. Requiere rol ADMIN o RECEPCIONISTA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Congelamiento finalizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CongelamientoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "El congelamiento ya terminó", content = @Content),
            @ApiResponse(responseCode = "404", description = "Congelamiento no encontrado", content = @Content)
    })
    @PostMapping("/congelamientos/{idCongelamiento}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<CongelamientoResponseDto> finalizar(
            @PathVariable Integer idCongelamiento,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(congelamientoService.finalizar(idCongelamiento, currentUser));
    }

    @Operation(summary = "Congelamientos del socio autenticado (/me)", description = "Para que el socio vea desde su portal cuándo estuvo o estará congelada su membresía.")
    @GetMapping("/me/congelamientos")
    public ResponseEntity<List<CongelamientoResponseDto>> misCongelamientos(@CurrentUser User currentUser) {
        return ResponseEntity.ok(congelamientoService.listarPorSocio(currentUser.getId()));
    }
}
