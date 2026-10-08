package com.example.backendgymteo1.dto.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de respuesta detallado con la información completa de un pago")
public class PagoResponseDto {

    @Schema(description = "ID del comprobante de pago", example = "100")
    private Integer idComprobante;

    @Schema(description = "ID de la factura asociada", example = "10")
    private Integer idFactura;

    @Schema(description = "ID del socio", example = "4")
    private Integer idSocio;

    @Schema(description = "Nombre completo del socio", example = "Juan Pérez")
    private String nombreSocio;

    @Schema(description = "Correo del socio", example = "juan.perez@example.com")
    private String correoSocio;

    @Schema(description = "DPI del socio", example = "1234567890101")
    private String dpiSocio;

    @Schema(description = "ID de la membresía", example = "1")
    private Integer idMembresia;

    @Schema(description = "Nombre del plan contratado", example = "Plan Mensual")
    private String nombrePlan;

    @Schema(description = "Duración del plan en días", example = "30")
    private Integer duracionPlan;

    @Schema(description = "Nueva fecha de vencimiento recalculada tras el pago", example = "2026-11-07")
    private LocalDate nuevaFechaVencimiento;

    @Schema(description = "Método de pago utilizado")
    private MetodoPagoResponseDto metodoPago;

    @Schema(description = "Fecha y hora en que se registró el pago", example = "2026-10-08T15:30:00")
    private LocalDateTime fechaPago;

    @Schema(description = "Monto total pagado", example = "250.00")
    private BigDecimal montoPagado;

    @Schema(description = "Referencia o voucher del pago", example = "REF-12345")
    private String referencia;

    @Schema(description = "Nombre del recepcionista que procesó el pago", example = "María López")
    private String nombreRecepcionista;

    @Schema(description = "Observación o nota del pago", example = "Pago exitoso")
    private String observacion;
}
