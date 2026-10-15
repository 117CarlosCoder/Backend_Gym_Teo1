package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.pago.CotizacionPagoDto;
import com.example.backendgymteo1.entity.*;
import com.example.backendgymteo1.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AvisoPagoServiceTest {
    private NotificacionRepository repository;
    private EmailService email;
    private Factura factura;
    private CotizacionPagoDto cotizacion;

    @BeforeEach
    void preparar() {
        repository = mock(NotificacionRepository.class);
        email = mock(EmailService.class);
        User usuario = User.builder().id(4).correo("socio@example.com").build();
        Membresia membresia = Membresia.builder().id(10)
                .socio(Socio.builder().usuario(usuario).build())
                .plan(PlanMembresia.builder().nombre("Semanal").build()).build();
        factura = Factura.builder().id(20).membresia(membresia)
                .montoBase(new BigDecimal("200.00")).descuento(new BigDecimal("50.00"))
                .recargo(new BigDecimal("25.00")).monto(new BigDecimal("175.00")).build();
        cotizacion = CotizacionPagoDto.builder().nombrePromocion("Promo vigente")
                .porcentajeRecargo(new BigDecimal("12.50")).diasAtraso(23).build();
    }

    private AnnotationConfigApplicationContext contexto() {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean(NotificacionRepository.class, () -> repository);
        context.registerBean(EmailService.class, () -> email);
        context.registerBean(Clock.class, Clock::systemUTC);
        context.registerBean(org.springframework.transaction.event.TransactionalEventListenerFactory.class);
        context.registerBean(AvisoPagoService.class);
        context.refresh();
        return context;
    }

    // Sin BD: activa las mismas sincronizaciones de commit/rollback que usa Spring con JPA.
    private TransactionTemplate transaccion() {
        return new TransactionTemplate(new AbstractPlatformTransactionManager() {
            @Override protected Object doGetTransaction() { return new Object(); }
            @Override protected void doBegin(Object tx, TransactionDefinition definition) { }
            @Override protected void doCommit(DefaultTransactionStatus status) { }
            @Override protected void doRollback(DefaultTransactionStatus status) { }
        });
    }

    @Test
    void guardaDesgloseYEnviaCorreoSoloDespuesDelCommit() {
        try (var context = contexto()) {
            var service = context.getBean(AvisoPagoService.class);
            transaccion().executeWithoutResult(status -> {
                service.registrar(factura, cotizacion);
                verifyNoInteractions(email);
            });
            var captor = ArgumentCaptor.forClass(Notificacion.class);
            verify(repository).save(captor.capture());
            var aviso = captor.getValue();
            assertEquals(4, aviso.getUsuario().getId());
            assertEquals(TipoNotificacion.AVISO, aviso.getTipo());
            assertFalse(aviso.isLeida());
            assertNull(aviso.getUmbralDias());
            assertTrue(aviso.getMensaje().contains("Factura #20"));
            assertTrue(aviso.getMensaje().contains("Descuento: Q50.00 (Promo vigente)"));
            assertTrue(aviso.getMensaje().contains("Q25.00 (12.50%; 23 dia(s)"));
            assertTrue(aviso.getMensaje().contains("Total facturado: Q175.00"));
            verify(email).enviarAvisoRecargo("socio@example.com", aviso.getTitulo(), aviso.getMensaje());
        }
    }

    @Test
    void rollbackNoEnviaCorreo() {
        try (var context = contexto()) {
            transaccion().executeWithoutResult(status -> {
                context.getBean(AvisoPagoService.class).registrar(factura, cotizacion);
                status.setRollbackOnly();
            });
            verifyNoInteractions(email);
        }
    }

    @Test
    void descuentoSinRecargoSoloNotificaEnBandeja() {
        factura.setRecargo(BigDecimal.ZERO);
        try (var context = contexto()) {
            transaccion().executeWithoutResult(status -> context.getBean(AvisoPagoService.class).registrar(factura, cotizacion));
            verify(repository).save(argThat(n -> n.getTitulo().equals("Descuento aplicado a tu pago")));
            verifyNoInteractions(email);
        }
    }

    @Test
    void recargoSinDescuentoTambienNotificaYEnviaCorreo() {
        factura.setDescuento(BigDecimal.ZERO);
        try (var context = contexto()) {
            transaccion().executeWithoutResult(status -> context.getBean(AvisoPagoService.class).registrar(factura, cotizacion));
            verify(repository).save(argThat(n -> n.getTitulo().equals("Recargo aplicado a tu pago")));
            verify(email).enviarAvisoRecargo(eq("socio@example.com"), eq("Recargo aplicado a tu pago"), anyString());
        }
    }

    @Test
    void pagoSinAjustesNoGeneraAvisos() {
        factura.setDescuento(BigDecimal.ZERO);
        factura.setRecargo(BigDecimal.ZERO);
        try (var context = contexto()) {
            transaccion().executeWithoutResult(status -> context.getBean(AvisoPagoService.class).registrar(factura, cotizacion));
            verifyNoInteractions(repository, email);
        }
    }
}
