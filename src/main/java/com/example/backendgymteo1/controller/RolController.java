package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.dto.role.RolResponseDto;
import com.example.backendgymteo1.service.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Roles", description = "Consulta del catálogo de roles del gimnasio")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RolController {

    private final RolService rolService;

    @Operation(summary = "Listar todos los roles", description = "Retorna el catálogo completo de roles disponibles para asignación y registro de usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catálogo de roles obtenido exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RolResponseDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<RolResponseDto>> findAll() {
        return ResponseEntity.ok(rolService.findAll());
    }
}
