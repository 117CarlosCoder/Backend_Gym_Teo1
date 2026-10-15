package com.example.backendgymteo1.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Distribución de ingresos por método de pago")
public class IngresoMetodoPagoDto {

    @Schema(description = "Nombre del método de pago", example = "TARJETA_CREDITO")
    private String metodoPago;

    @Schema(description = "Monto total recaudado con este método", example = "2500.00")
    private BigDecimal total;

    @Schema(description = "Cantidad de pagos procesados", example = "10")
    private Long cantidad;

    @Schema(description = "Porcentaje respecto al total del período", example = "55.56")
    private Double porcentaje;
}
