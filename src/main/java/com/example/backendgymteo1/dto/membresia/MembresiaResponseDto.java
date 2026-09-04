package com.example.backendgymteo1.dto.membresia;

import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.dto.sucursal.SucursalResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos de respuesta detallada de una membresía de socio con soporte multisucursal")
public class MembresiaResponseDto {

    @Schema(description = "Identificador único de la membresía", example = "1")
    private Integer id;

    @Schema(description = "Datos del socio asociado")
    private SocioResponseDto socio;

    @Schema(description = "Plan / Tipo de membresía contratado")
    private PlanMembresiaResponseDto plan;

    @Schema(description = "Estado actual de la membresía", example = "ACTIVA")
    private EstadoMembresiaResponseDto estado;

    @Schema(description = "Fecha de inicio de la vigencia", example = "2026-03-01")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha de vencimiento calculada", example = "2026-03-31")
    private LocalDate fechaVencimiento;

    @Schema(description = "Indica si la membresía se encuentra activa actualmente", example = "true")
    private boolean activa;

    @Schema(description = "Días restantes de vigencia a partir de hoy", example = "27")
    private Long diasRestantes;

    @Schema(description = "Sucursales autorizadas para el uso de esta membresía")
    private List<SucursalResponseDto> sucursales;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Fecha y hora en que fue cancelada la membresía", example = "2026-09-04T10:00:00")
    private LocalDateTime fechaCancelacion;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Nombre o correo del usuario administrativo que procesó la cancelación", example = "Carlos Raúl López (Admin)")
    private String usuarioCancelo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Motivo de la cancelación", example = "Solicitud del cliente por cambio de domicilio")
    private String motivoCancelacion;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Comentarios u observaciones de la cancelación", example = "Se autorizó reembolso parcial")
    private String comentariosCancelacion;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Indica si aplicó reembolso al cancelar", example = "true")
    private Boolean reembolso;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Monto reembolsado al socio", example = "150.00")
    private BigDecimal montoReembolso;
}
