package com.example.backendgymteo1.dto.asistencia;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Métricas agregadas de asistencia en un rango de fechas")
public class MetricasAsistenciaResponseDto {

    public enum Agrupacion {
        DIA,
        SEMANA,
        MES
    }

    @Schema(description = "Fecha inicial del rango (inclusive)", example = "2026-10-01")
    private LocalDate desde;

    @Schema(description = "Fecha final del rango (inclusive)", example = "2026-10-31")
    private LocalDate hasta;

    @Schema(description = "Agrupación aplicada a la serie temporal", example = "SEMANA")
    private Agrupacion agrupacion;

    @Schema(description = "ID de sucursal usada como filtro (null = todas)", example = "1")
    private Integer idSucursal;

    @Schema(description = "Total de asistencias en el rango", example = "120")
    private long totalAsistencias;

    @Schema(description = "Socios distintos que asistieron en el rango", example = "45")
    private long sociosUnicos;

    @Schema(description = "Promedio de asistencias por día del rango", example = "3.87")
    private BigDecimal promedioDiario;

    @Schema(description = "Serie temporal de asistencias (incluye períodos con 0 asistencias)")
    private List<PuntoSerie> serie;

    @Schema(description = "Asistencias por sucursal, de mayor a menor")
    private List<AsistenciaSucursal> porSucursal;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Punto de la serie temporal")
    public static class PuntoSerie {

        @Schema(description = "Período: yyyy-MM-dd (DIA), yyyy-'W'ww ISO (SEMANA) o yyyy-MM (MES)", example = "2026-W41")
        private String periodo;

        @Schema(description = "Asistencias del período", example = "28")
        private long total;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Asistencias acumuladas de una sucursal")
    public static class AsistenciaSucursal {

        @Schema(description = "ID de la sucursal (null para asistencias históricas sin sucursal)", example = "1")
        private Integer idSucursal;

        @Schema(description = "Nombre de la sucursal", example = "Sucursal Central")
        private String nombreSucursal;

        @Schema(description = "Asistencias de la sucursal", example = "75")
        private long total;
    }
}
