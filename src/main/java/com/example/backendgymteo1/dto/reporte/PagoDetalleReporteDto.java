package com.example.backendgymteo1.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle de pago registrado en el reporte de ingresos")
public class PagoDetalleReporteDto {

    @Schema(description = "ID del comprobante de pago", example = "105")
    private Integer comprobanteId;

    @Schema(description = "ID del socio pagador", example = "10")
    private Integer socioId;

    @Schema(description = "Nombre completo del socio", example = "Carlos López")
    private String nombreSocio;

    @Schema(description = "DPI del socio", example = "2987123450101")
    private String dpiSocio;

    @Schema(description = "Correo del socio", example = "carlos@gym.com")
    private String correoSocio;

    @Schema(description = "Plan contratado asociado al pago", example = "Plan Mensual")
    private String planNombre;

    @Schema(description = "Método de pago utilizado", example = "TARJETA_CREDITO")
    private String metodoPago;

    @Schema(description = "Monto cancelado", example = "350.00")
    private BigDecimal montoPagado;

    @Schema(description = "Fecha y hora del pago", example = "2026-10-15T08:30:00")
    private LocalDateTime fechaPago;

    @Schema(description = "Número o identificador de factura", example = "FAC-2026-00105")
    private String numeroFactura;

    @Schema(description = "Referencia de transacción o comprobante", example = "AUTH-789012")
    private String referencia;
}
