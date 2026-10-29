package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.configuracion.ReglasCongelamientoDto;
import com.example.backendgymteo1.dto.membresia.CongelamientoResponseDto;
import com.example.backendgymteo1.dto.membresia.CreateCongelamientoDto;
import com.example.backendgymteo1.entity.CongelamientoMembresia;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.CongelamientoMembresiaRepository;
import com.example.backendgymteo1.repository.EstadoMembresiaRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CongelamientoServiceTest {

    @Mock
    private CongelamientoMembresiaRepository congelamientoRepository;
    @Mock
    private MembresiaRepository membresiaRepository;
    @Mock
    private EstadoMembresiaRepository estadoMembresiaRepository;
    @Mock
    private ConfiguracionSistemaService configuracionService;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private CongelamientoService service;

    private final LocalDate hoy = LocalDate.now();
    private final EstadoMembresia activa = EstadoMembresia.builder().id(1).nombre("ACTIVA").build();
    private final EstadoMembresia congelada = EstadoMembresia.builder().id(3).nombre("CONGELADA").build();
    private final User recepcion = User.builder().id(2).nombres("María").apellidos("Castro").build();
    private Membresia membresia;

    @BeforeEach
    void setUp() {
        PlanMembresia plan = PlanMembresia.builder().id(1).nombre("Plan Mensual").duracion(30).precio(new BigDecimal("250.00")).build();
        membresia = Membresia.builder()
                .id(10).plan(plan).estadoMembresia(activa)
                .fechaInicio(hoy.minusDays(10)).fechaVencimiento(hoy.plusDays(20))
                .build();
        lenient().when(configuracionService.consultarReglasCongelamiento())
                .thenReturn(ReglasCongelamientoDto.builder().diasMinimo(7).diasMaximo(30).maximoPorAnio(2).build());
        lenient().when(membresiaRepository.findByIdWithDetails(10)).thenReturn(Optional.of(membresia));
        lenient().when(congelamientoRepository.save(any(CongelamientoMembresia.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(estadoMembresiaRepository.findById(1)).thenReturn(Optional.of(activa));
        lenient().when(estadoMembresiaRepository.findById(3)).thenReturn(Optional.of(congelada));
    }

    private CreateCongelamientoDto solicitud(LocalDate inicio, LocalDate fin) {
        return CreateCongelamientoDto.builder().fechaInicio(inicio).fechaFin(fin).motivo("Viaje").build();
    }

    @Test
    @DisplayName("Congelar desde hoy suma los días al vencimiento y deja la membresía CONGELADA")
    void congelarDesdeHoyExtiendeVencimiento() {
        CongelamientoResponseDto r = service.congelar(10, solicitud(hoy, hoy.plusDays(9)), recepcion);

        assertEquals(10, r.getDias());
        assertEquals("ACTIVO", r.getEstado());
        assertEquals(hoy.plusDays(30), membresia.getFechaVencimiento());
        assertEquals("CONGELADA", membresia.getEstadoMembresia().getNombre());
        verify(membresiaRepository).save(membresia);
    }

    @Test
    @DisplayName("Un congelamiento programado extiende el vencimiento pero la membresía sigue ACTIVA hasta que inicie")
    void congelamientoProgramado() {
        CongelamientoResponseDto r = service.congelar(10, solicitud(hoy.plusDays(5), hoy.plusDays(11)), recepcion);

        assertEquals("PROGRAMADO", r.getEstado());
        assertEquals(hoy.plusDays(27), membresia.getFechaVencimiento());
        assertEquals("ACTIVA", membresia.getEstadoMembresia().getNombre());
    }

    @Test
    @DisplayName("Rechaza congelamientos fuera del rango de días permitido")
    void rechazaDuracionFueraDeReglas() {
        RuntimeException corto = assertThrows(RuntimeException.class,
                () -> service.congelar(10, solicitud(hoy, hoy.plusDays(2)), recepcion));
        assertTrue(corto.getMessage().contains("entre 7 y 30"));

        RuntimeException largo = assertThrows(RuntimeException.class,
                () -> service.congelar(10, solicitud(hoy, hoy.plusDays(40)), recepcion));
        assertTrue(largo.getMessage().contains("entre 7 y 30"));
        verify(congelamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza superar la cantidad de congelamientos por año")
    void rechazaMaximoPorAnio() {
        when(congelamientoRepository.contarEnAnio(10, hoy.getYear())).thenReturn(2L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.congelar(10, solicitud(hoy, hoy.plusDays(9)), recepcion));
        assertTrue(ex.getMessage().contains("máximo de 2"));
    }

    @Test
    @DisplayName("Rechaza fechas pasadas, traslapes y membresías no activas o vencidas")
    void rechazaCasosInvalidos() {
        assertThrows(RuntimeException.class, () -> service.congelar(10, solicitud(hoy.minusDays(1), hoy.plusDays(8)), recepcion));

        when(congelamientoRepository.existeTraslape(eq(10), any(), any())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.congelar(10, solicitud(hoy, hoy.plusDays(9)), recepcion));

        membresia.setEstadoMembresia(congelada);
        RuntimeException yaCongelada = assertThrows(RuntimeException.class,
                () -> service.congelar(10, solicitud(hoy, hoy.plusDays(9)), recepcion));
        assertTrue(yaCongelada.getMessage().contains("ya se encuentra congelada"));

        membresia.setEstadoMembresia(activa);
        membresia.setFechaVencimiento(hoy.minusDays(1));
        RuntimeException vencida = assertThrows(RuntimeException.class,
                () -> service.congelar(10, solicitud(hoy, hoy.plusDays(9)), recepcion));
        assertTrue(vencida.getMessage().contains("venció"));
    }

    @Test
    @DisplayName("Descongelar antes de tiempo devuelve los días no usados y reactiva la membresía")
    void finalizarAnticipadamente() {
        membresia.setEstadoMembresia(congelada);
        membresia.setFechaVencimiento(hoy.plusDays(40));
        CongelamientoMembresia c = CongelamientoMembresia.builder()
                .id(5).membresia(membresia).fechaInicio(hoy.minusDays(4)).fechaFin(hoy.plusDays(5)).dias(10)
                .fechaRegistro(LocalDateTime.now()).build();
        when(congelamientoRepository.findByIdConMembresia(5)).thenReturn(Optional.of(c));

        CongelamientoResponseDto r = service.finalizar(5, recepcion);

        assertEquals(4, r.getDias());
        assertEquals(hoy.minusDays(1), r.getFechaFin());
        assertEquals("FINALIZADO", r.getEstado());
        assertEquals(hoy.plusDays(34), membresia.getFechaVencimiento());
        assertEquals("ACTIVA", membresia.getEstadoMembresia().getNombre());
    }

    @Test
    @DisplayName("Cancelar un congelamiento programado lo anula y quita todos sus días")
    void anularProgramado() {
        membresia.setFechaVencimiento(hoy.plusDays(30));
        CongelamientoMembresia c = CongelamientoMembresia.builder()
                .id(6).membresia(membresia).fechaInicio(hoy.plusDays(3)).fechaFin(hoy.plusDays(12)).dias(10)
                .fechaRegistro(LocalDateTime.now()).build();
        when(congelamientoRepository.findByIdConMembresia(6)).thenReturn(Optional.of(c));

        CongelamientoResponseDto r = service.finalizar(6, recepcion);

        assertEquals("ANULADO", r.getEstado());
        assertEquals(hoy.plusDays(20), membresia.getFechaVencimiento());
    }

    @Test
    @DisplayName("Un congelamiento ya terminado no se puede finalizar")
    void noFinalizaTerminado() {
        CongelamientoMembresia c = CongelamientoMembresia.builder()
                .id(7).membresia(membresia).fechaInicio(hoy.minusDays(20)).fechaFin(hoy.minusDays(11)).dias(10).build();
        when(congelamientoRepository.findByIdConMembresia(7)).thenReturn(Optional.of(c));

        assertThrows(RuntimeException.class, () -> service.finalizar(7, recepcion));
    }

    @Test
    @DisplayName("El check-in se bloquea mientras haya un congelamiento en curso")
    void bloqueaIngresoCongelado() {
        CongelamientoMembresia c = CongelamientoMembresia.builder()
                .fechaInicio(hoy.minusDays(1)).fechaFin(hoy.plusDays(8)).dias(10).build();
        when(congelamientoRepository.findActivosBySocio(4, hoy)).thenReturn(List.of(c));
        when(congelamientoRepository.findActivosBySocio(5, hoy)).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.validarSinCongelamientoActivo(4, hoy));
        assertTrue(ex.getMessage().contains("congelada"));
        assertDoesNotThrow(() -> service.validarSinCongelamientoActivo(5, hoy));
    }

    @Test
    @DisplayName("La sincronización diaria congela las programadas que inician y reactiva las que terminaron")
    void sincronizaEstados() {
        Membresia porIniciar = Membresia.builder().id(20).estadoMembresia(activa).build();
        Membresia terminada = Membresia.builder().id(21).estadoMembresia(congelada).build();
        CongelamientoMembresia c = CongelamientoMembresia.builder()
                .membresia(porIniciar).fechaInicio(hoy).fechaFin(hoy.plusDays(9)).dias(10).build();
        when(congelamientoRepository.findActivosEn(hoy)).thenReturn(List.of(c));
        when(membresiaRepository.findByEstadoMembresia_Id(3)).thenReturn(List.of(terminada));
        when(congelamientoRepository.existeActivoEn(anyInt(), eq(hoy))).thenReturn(false);

        int cambios = service.sincronizarEstados(hoy);

        assertEquals(2, cambios);
        assertEquals("CONGELADA", porIniciar.getEstadoMembresia().getNombre());
        assertEquals("ACTIVA", terminada.getEstadoMembresia().getNombre());
        assertFalse(c.isAnulado());
    }
}
