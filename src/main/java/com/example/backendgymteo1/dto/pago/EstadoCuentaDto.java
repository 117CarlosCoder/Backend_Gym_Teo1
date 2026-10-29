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
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Estado de cuenta del socio: lo facturado, lo pagado y el saldo pendiente")
public class EstadoCuentaDto {

    private Integer idSocio;
    private String nombreSocio;

    @Schema(description = "Suma de las facturas no anuladas", example = "900.00")
    private BigDecimal totalFacturado;

    @Schema(description = "Suma de todos los pagos y abonos", example = "750.00")
    private BigDecimal totalPagado;

    @Schema(description = "Saldo que falta pagar", example = "150.00")
    private BigDecimal saldoPendiente;

    @Schema(description = "Parte del saldo cuyo plazo ya venció", example = "0.00")
    private BigDecimal saldoVencido;

    @Schema(description = "Fecha límite más próxima para liquidar el saldo", example = "2026-11-13")
    private LocalDate proximaFechaLimite;

    private List<FacturaCuentaDto> facturas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Factura dentro del estado de cuenta")
    public static class FacturaCuentaDto {
        private Integer idFactura;
        private Integer idMembresia;
        private String nombrePlan;
        private LocalDate fechaEmision;
        @Schema(description = "Fecha límite para liquidar el saldo (si se pagó en abonos)")
        private LocalDate fechaLimite;
        private BigDecimal montoBase;
        private BigDecimal descuento;
        private BigDecimal recargo;
        private BigDecimal total;
        private BigDecimal pagado;
        private BigDecimal saldo;
        @Schema(description = "PAGADA, PENDIENTE, VENCIDA (saldo fuera de plazo) o ANULADA", example = "PENDIENTE")
        private String estado;
        private List<AbonoDto> abonos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Pago o abono registrado en una factura")
    public static class AbonoDto {
        private Integer idComprobante;
        private LocalDateTime fechaPago;
        private BigDecimal monto;
        private String metodoPago;
        private String referencia;
    }
}
