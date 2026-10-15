package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.promocion.CreatePromocionDto;
import com.example.backendgymteo1.dto.promocion.PromocionResponseDto;
import com.example.backendgymteo1.dto.promocion.UpdatePromocionDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.PromocionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

@Tag(name = "Promociones", description = "Gestión de promociones de descuento. La mejor promoción vigente se aplica automáticamente al registrar un pago")
@RestController
@RequestMapping("/promociones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PromocionController {

    private final PromocionService promocionService;

    @Operation(summary = "Listar todas las promociones", description = "Incluye promociones activas, inactivas y vencidas. Requiere rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de promociones",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PromocionResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromocionResponseDto>> findAll() {
        return ResponseEntity.ok(promocionService.findAll());
    }

    @Operation(summary = "Listar promociones vigentes hoy", description = "Promociones activas cuyo rango de fechas incluye el día de hoy. Requiere rol ADMIN o RECEPCIONISTA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promociones vigentes",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PromocionResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN o RECEPCIONISTA)", content = @Content)
    })
    @GetMapping("/vigentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<PromocionResponseDto>> findVigentes() {
        return ResponseEntity.ok(promocionService.findVigentes());
    }

    @Operation(summary = "Obtener una promoción por ID", description = "Requiere rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promoción encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromocionResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Promoción no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromocionResponseDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(promocionService.findById(id));
    }

    @Operation(summary = "Crear una promoción", description = "Define un descuento por PORCENTAJE (0-100) o MONTO_FIJO con rango de vigencia. Requiere rol ADMIN. Registra evento en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Promoción creada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromocionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, nombre duplicado o fechas incoherentes", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromocionResponseDto> create(
            @Valid @RequestBody CreatePromocionDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promocionService.create(request, currentUser));
    }

    @Operation(summary = "Actualizar una promoción", description = "Actualización parcial: solo se modifican los campos enviados. Requiere rol ADMIN. Registra evento en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promoción actualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromocionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, nombre duplicado o fechas incoherentes", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Promoción no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromocionResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdatePromocionDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(promocionService.update(id, request, currentUser));
    }

    @Operation(summary = "Eliminar una promoción", description = "No se puede eliminar si ya fue aplicada en facturas (desactívela con PUT). Requiere rol ADMIN. Registra evento en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Promoción eliminada"),
            @ApiResponse(responseCode = "400", description = "La promoción ya fue usada en facturas", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Promoción no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @CurrentUser User currentUser) {
        promocionService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
