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

    @Schema(description = "Precio base del plan al momento del pago", example = "250.00")
    private BigDecimal montoBase;

    @Schema(description = "Descuento aplicado por promoción", example = "25.00")
    private BigDecimal descuento;

    @Schema(description = "Recargo aplicado por pago tardío", example = "0.00")
    private BigDecimal recargo;

    @Schema(description = "ID de la promoción aplicada", example = "1")
    private Integer idPromocion;

    @Schema(description = "Nombre de la promoción aplicada como descuento", example = "Promo Estudiantes", nullable = true)
    private String nombrePromocion;

    @Schema(description = "Referencia o voucher del pago", example = "REF-12345")
    private String referencia;

    @Schema(description = "Nombre del recepcionista que procesó el pago", example = "María López")
    private String nombreRecepcionista;

    @Schema(description = "Observación o nota del pago", example = "Pago exitoso")
    private String observacion;

    // --- Pagos en cuotas / abonos ---

    @Schema(description = "Total de la factura (precio - descuento + recargo)", example = "250.00")
    private BigDecimal totalFactura;

    @Schema(description = "PAGADA o PENDIENTE (con saldo)", example = "PENDIENTE")
    private String estadoFactura;

    @Schema(description = "Número de pago dentro de la factura (1 = pago inicial)", example = "2")
    private Integer numeroAbono;

    @Schema(description = "Pagado en la factura hasta este comprobante, incluido", example = "200.00")
    private BigDecimal totalAbonado;

    @Schema(description = "Saldo que quedó pendiente después de este comprobante", example = "50.00")
    private BigDecimal saldoPendiente;

    @Schema(description = "Fecha límite para liquidar el saldo", example = "2026-11-13")
    private LocalDate fechaLimiteSaldo;
}
