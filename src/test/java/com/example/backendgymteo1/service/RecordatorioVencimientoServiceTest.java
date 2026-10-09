package com.example.backendgymteo1.service;

import com.example.backendgymteo1.config.ReminderProperties;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.EstadoRecordatorioCorreo;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.Notificacion;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.RecordatorioCorreo;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.MembresiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordatorioVencimientoServiceTest {

    @Mock
    private MembresiaRepository membresiaRepository;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private RecordatorioCorreoService recordatorioCorreoService;

    @Mock
    private EmailService emailService;

    @Mock
    private ReminderProperties reminderProperties;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-10-09T10:00:00Z"), ZoneId.of("America/Guatemala"));

    @InjectMocks
    private RecordatorioVencimientoService recordatorioVencimientoService;

    private User userVigente;
    private Socio socioVigente;
    private PlanMembresia plan;
    private EstadoMembresia estadoActiva;
    private Membresia membresiaActiva;

    @BeforeEach
    void setUp() {
        userVigente = User.builder()
                .id(10)
                .nombres("Carlos")
                .apellidos("López")
                .correo("clp64413@gmail.com")
                .estado(true)
                .eliminadoEn(null)
                .build();

        socioVigente = Socio.builder()
                .id(10)
                .usuario(userVigente)
                .activo(true)
                .eliminadoEn(null)
                .build();

        plan = PlanMembresia.builder()
                .id(1)
                .nombre("Plan Mensual")
                .build();

        estadoActiva = EstadoMembresia.builder()
                .id(1)
                .nombre("ACTIVA")
                .build();

        membresiaActiva = Membresia.builder()
                .id(100)
                .socio(socioVigente)
                .plan(plan)
                .estadoMembresia(estadoActiva)
                .fechaInicio(LocalDate.of(2026, 9, 12))
                .fechaVencimiento(LocalDate.of(2026, 10, 12))
                .build();
    }

    @Test
    @DisplayName("Genera recordatorios para membresías activas dentro del umbral de días")
    void testGenerarRecordatoriosParaFecha_Exito() {
        LocalDate hoy = LocalDate.of(2026, 10, 9);
        LocalDate maxFecha = hoy.plusDays(7);

        when(reminderProperties.getDays()).thenReturn(7);
        when(membresiaRepository.findMembresiasParaRecordatorio(hoy, maxFecha))
                .thenReturn(List.of(membresiaActiva));
        when(notificacionService.crearNotificacionYRecordatorioEnTransaccion(eq(membresiaActiva), eq(membresiaActiva.getFechaVencimiento()), eq(7), eq(3)))
                .thenReturn(Optional.of(new Notificacion()));

        recordatorioVencimientoService.generarRecordatoriosParaFecha(hoy);

        verify(notificacionService).crearNotificacionYRecordatorioEnTransaccion(membresiaActiva, membresiaActiva.getFechaVencimiento(), 7, 3);
    }

    @Test
    @DisplayName("Despacha recordatorio por correo exitosamente cuando el socio y membresía están activos")
    void testProcesarEnviosPendientes_DespachoExitoso() {
        when(recordatorioCorreoService.obtenerCandidatosParaEnvio(anyInt(), any(LocalDateTime.class)))
                .thenReturn(List.of(1));
        when(recordatorioCorreoService.reclamarCandidatoParaEnvio(eq(1), any(LocalDateTime.class), anyString()))
                .thenReturn(true);

        Notificacion notif = Notificacion.builder()
                .id(20)
                .fechaVencimiento(LocalDate.of(2026, 10, 12))
                .build();

        RecordatorioCorreo recordatorio = RecordatorioCorreo.builder()
                .id(1)
                .notificacion(notif)
                .membresia(membresiaActiva)
                .usuario(userVigente)
                .destinatario("clp64413@gmail.com")
                .nombreSocioSnapshot("Carlos López")
                .nombrePlanSnapshot("Plan Mensual")
                .diasRestantesSnapshot(3)
                .estado(EstadoRecordatorioCorreo.PENDIENTE)
                .idempotencyKey("remind-100-key")
                .fechaCreacion(LocalDateTime.of(2026, 10, 9, 8, 0))
                .intentos(0)
                .maxIntentos(3)
                .build();

        when(recordatorioCorreoService.obtenerConDetalles(1)).thenReturn(Optional.of(recordatorio));
        when(membresiaRepository.findByIdWithDetails(100)).thenReturn(Optional.of(membresiaActiva));
        when(emailService.enviarRecordatorioVencimiento(
                eq("clp64413@gmail.com"),
                eq("Carlos López"),
                eq("Plan Mensual"),
                eq(LocalDate.of(2026, 10, 12)),
                eq(3L),
                eq("remind-100-key")
        )).thenReturn("resend-msg-12345");

        recordatorioVencimientoService.procesarEnviosPendientes();

        verify(emailService).enviarRecordatorioVencimiento(
                eq("clp64413@gmail.com"),
                eq("Carlos López"),
                eq("Plan Mensual"),
                eq(LocalDate.of(2026, 10, 12)),
                eq(3L),
                eq("remind-100-key")
        );
        verify(recordatorioCorreoService).marcarExito(eq(1), anyString(), eq("resend-msg-12345"));
    }

    @Test
    @DisplayName("Marca recordatorio como OBSOLETO y NO envía correo si el usuario fue eliminado (soft delete)")
    void testProcesarEnviosPendientes_UsuarioEliminado_MarcaObsoleto() {
        when(recordatorioCorreoService.obtenerCandidatosParaEnvio(anyInt(), any(LocalDateTime.class)))
                .thenReturn(List.of(2));
        when(recordatorioCorreoService.reclamarCandidatoParaEnvio(eq(2), any(LocalDateTime.class), anyString()))
                .thenReturn(true);

        // Usuario eliminado lógicamente
        User userEliminado = User.builder()
                .id(10)
                .nombres("Carlos")
                .apellidos("López")
                .correo(null)
                .estado(false)
                .eliminadoEn(LocalDateTime.of(2026, 10, 9, 9, 0))
                .build();

        Socio socioEliminado = Socio.builder()
                .id(10)
                .usuario(userEliminado)
                .activo(false)
                .eliminadoEn(LocalDateTime.of(2026, 10, 9, 9, 0))
                .build();

        Membresia membresiaConUserEliminado = Membresia.builder()
                .id(101)
                .socio(socioEliminado)
                .plan(plan)
                .estadoMembresia(estadoActiva)
                .fechaInicio(LocalDate.of(2026, 9, 12))
                .fechaVencimiento(LocalDate.of(2026, 10, 12))
                .build();

        Notificacion notif = Notificacion.builder()
                .id(21)
                .fechaVencimiento(LocalDate.of(2026, 10, 12))
                .build();

        RecordatorioCorreo recordatorio = RecordatorioCorreo.builder()
                .id(2)
                .notificacion(notif)
                .membresia(membresiaConUserEliminado)
                .usuario(userEliminado)
                .destinatario("clp64413@gmail.com")
                .nombreSocioSnapshot("Carlos López")
                .nombrePlanSnapshot("Plan Mensual")
                .diasRestantesSnapshot(3)
                .estado(EstadoRecordatorioCorreo.PENDIENTE)
                .idempotencyKey("remind-101-key")
                .fechaCreacion(LocalDateTime.of(2026, 10, 9, 8, 0))
                .intentos(0)
                .maxIntentos(3)
                .build();

        when(recordatorioCorreoService.obtenerConDetalles(2)).thenReturn(Optional.of(recordatorio));
        when(membresiaRepository.findByIdWithDetails(101)).thenReturn(Optional.of(membresiaConUserEliminado));

        recordatorioVencimientoService.procesarEnviosPendientes();

        verify(recordatorioCorreoService).marcarObsoleto(eq(2), anyString(), anyString());
        verify(emailService, never()).enviarRecordatorioVencimiento(any(), any(), any(), any(), anyLong(), any());
    }
}
