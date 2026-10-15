package com.example.backendgymteo1.dto.reporte;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Métricas de asistencia consolidadas según el rol del usuario")
public class MetricaAsistenciaDto {

    @Schema(description = "Rol del usuario con el que se generaron las métricas", example = "ADMIN")
    private String rol;

    @Schema(description = "Total de asistencias hoy", example = "85")
    private Long asistenciasHoy;

    @Schema(description = "Total de asistencias en la semana en curso", example = "420")
    private Long asistenciasSemanaActual;

    @Schema(description = "Total de asistencias en el mes evaluado", example = "1750")
    private Long asistenciasMesActual;

    @Schema(description = "Promedio diario de asistencias en el mes", example = "58.33")
    private Double promedioDiarioMes;

    @Schema(description = "ID de la sucursal evaluada", example = "1")
    private Integer sucursalId;

    @Schema(description = "Nombre de la sucursal", example = "Sede Central")
    private String nombreSucursal;

    @Schema(description = "Asistencias registradas hoy por el usuario autenticado", example = "25")
    private Long registradasPorMiHoy;

    @Schema(description = "Horas pico de asistencia")
    private List<HoraPicoDto> horasPico;

    @Schema(description = "Distribución por día de la semana")
    private List<DiaSemanaAsistenciaDto> asistenciasPorDiaSemana;

    @Schema(description = "Distribución por sucursal")
    private List<SucursalAsistenciaDto> asistenciasPorSucursal;

    @Schema(description = "Top socios más frecuentes")
    private List<TopSocioAsistenciaDto> topSociosFrecuentes;

    @Schema(description = "Últimos registros de asistencia")
    private List<AsistenciaResumenDto> ultimosRegistros;

    @Schema(description = "ID del socio", example = "4")
    private Integer socioId;

    @Schema(description = "Nombre completo del socio", example = "Lucia Hernandez")
    private String nombreSocio;

    @Schema(description = "Total histórico de visitas", example = "58")
    private Long totalVisitasHistorico;

    @Schema(description = "Racha de días consecutivos recientes", example = "4")
    private Integer rachaDiasConsecutivos;

    @Schema(description = "Horario preferido habitual de entrenamiento", example = "Mañana (05:00 - 12:00)")
    private String horarioPreferido;

    @Schema(description = "Historial mensual de visitas")
    private List<VisitasMesDto> historialVisitasPorMes;

    @Schema(description = "Última asistencia registrada")
    private AsistenciaResumenDto ultimaAsistencia;
}
