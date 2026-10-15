package com.example.backendgymteo1.dto.reporte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte financiero consolidado de ingresos")
public class ReporteIngresosDto {

    @Schema(description = "Año consultado o null si es histórico", example = "2026")
    private Integer anio;

    @Schema(description = "Total acumulado en el período", example = "54200.00")
    private BigDecimal totalPeriodo;

    @Schema(description = "Total de pagos registrados en el período", example = "215")
    private Long cantidadPagosPeriodo;

    @Schema(description = "Ingreso promedio mensual en el período", example = "4516.67")
    private BigDecimal ingresoPromedioMensual;

    @Schema(description = "Mes con mayor ingreso", example = "Octubre 2026")
    private String mesMayorIngreso;

    @Schema(description = "Monto del mes con mayor ingreso", example = "8500.00")
    private BigDecimal montoMayorIngreso;

    @Schema(description = "Desglose cronológico de ingresos por mes")
    private List<IngresoMensualDto> ingresosPorMes;

    @Schema(description = "Desglose por método de pago")
    private List<IngresoMetodoPagoDto> ingresosPorMetodoPago;

    @Schema(description = "Últimos comprobantes de pago registrados con información del socio")
    private List<PagoDetalleReporteDto> ultimosPagos;
}
