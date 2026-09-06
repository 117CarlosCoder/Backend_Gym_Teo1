package com.example.backendgymteo1.dto.asistencia;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para registrar el ingreso / asistencia de un socio al gimnasio")
public class RegistrarAsistenciaDto {

    @NotNull(message = "El ID del socio (id_socio) es obligatorio")
    @JsonProperty("id_socio")
    @JsonAlias({"idSocio", "socioId", "id_socio"})
    @Schema(description = "Identificador único del socio", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer idSocio;

    @NotNull(message = "El ID de la sucursal (sucursal_id) es obligatorio")
    @JsonProperty("sucursal_id")
    @JsonAlias({"sucursalId", "idSucursal", "sucursal_id", "id_sucursal"})
    @Schema(description = "Identificador único de la sucursal", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sucursalId;
}
