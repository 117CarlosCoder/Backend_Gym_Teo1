package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.membresia.CreatePlanMembresiaDto;
import com.example.backendgymteo1.dto.membresia.PlanMembresiaResponseDto;
import com.example.backendgymteo1.dto.membresia.UpdatePlanMembresiaDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.PlanMembresiaService;
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

@Tag(name = "Planes de Membresía", description = "Operaciones CRUD para la consulta, creación, actualización y eliminación de planes y tarifas del gimnasio")
@RestController
@RequestMapping("/planes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlanMembresiaController {

    private final PlanMembresiaService planMembresiaService;

    @Operation(summary = "Listar todos los planes de membresía", description = "Retorna el catálogo completo de planes disponibles con sus duraciones y precios. Accesible para cualquier usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catálogo de planes obtenido exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PlanMembresiaResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlanMembresiaResponseDto>> findAll() {
        return ResponseEntity.ok(planMembresiaService.findAll());
    }

    @Operation(summary = "Obtener un plan de membresía por ID", description = "Retorna la información detallada de un plan específico por su ID. Accesible para usuarios autenticados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlanMembresiaResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlanMembresiaResponseDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(planMembresiaService.findById(id));
    }

    @Operation(summary = "Crear un nuevo plan de membresía", description = "Permite a un administrador dar de alta una nueva tarifa o plan (nombre, duración en días, precio y descripción). Requiere rol ADMIN. Registra evento en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plan de membresía creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlanMembresiaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o nombre de plan duplicado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanMembresiaResponseDto> create(
            @Valid @RequestBody CreatePlanMembresiaDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planMembresiaService.create(request, currentUser));
    }

    @Operation(summary = "Actualizar un plan de membresía existente", description = "Permite modificar nombre, duración, precio y descripción de un plan. Requiere rol ADMIN. Registra modificaciones en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan de membresía actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlanMembresiaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o nombre duplicado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanMembresiaResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdatePlanMembresiaDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.ok(planMembresiaService.update(id, request, currentUser));
    }

    @Operation(summary = "Eliminar un plan de membresía por ID", description = "Elimina un plan siempre y cuando no existan membresías históricas o activas que dependan de él. Requiere rol ADMIN. Registra evento en auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plan de membresía eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar porque tiene membresías asociadas", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @CurrentUser User currentUser) {
        planMembresiaService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
