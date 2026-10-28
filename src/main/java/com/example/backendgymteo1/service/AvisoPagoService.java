package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.pago.CotizacionPagoDto;
import com.example.backendgymteo1.entity.Factura;
import com.example.backendgymteo1.entity.Notificacion;
import com.example.backendgymteo1.entity.TipoNotificacion;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AvisoPagoService {

    private final NotificacionRepository notificacionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final Clock clock;

    @Transactional
    public void registrar(Factura factura, CotizacionPagoDto cotizacion) {
        boolean descuento = factura.getDescuento().signum() > 0;
        boolean recargo = factura.getRecargo().signum() > 0;
        if (!descuento && !recargo) {
            return;
        }

        User usuario = factura.getMembresia().getSocio().getUsuario();
        String titulo = descuento && recargo ? "Descuento y recargo aplicados a tu pago"
                : descuento ? "Descuento aplicado a tu pago" : "Recargo aplicado a tu pago";
        String promocion = (descuento && cotizacion.getNombrePromocion() != null && !cotizacion.getNombrePromocion().isBlank())
                ? " (" + cotizacion.getNombrePromocion() + ")"
                : "";
        String mensaje = String.format(Locale.ROOT,
                "Factura #%d del plan '%s'. Precio base: Q%.2f. Descuento: Q%.2f%s. "
                        + "Recargo por pago tardio: Q%.2f (%s%%; %d dia(s) de atraso). Total facturado: Q%.2f.",
                factura.getId(), factura.getMembresia().getPlan().getNombre(), factura.getMontoBase(),
                factura.getDescuento(), promocion, factura.getRecargo(),
                cotizacion.getPorcentajeRecargo().toPlainString(), cotizacion.getDiasAtraso(), factura.getMonto());

        notificacionRepository.save(Notificacion.builder()
                .usuario(usuario)
                .membresia(factura.getMembresia())
                .tipo(TipoNotificacion.AVISO)
                .titulo(titulo)
                .mensaje(mensaje)
                .leida(false)
                .fechaCreacion(LocalDateTime.now(clock))
                .build());

        if (recargo && usuario.getCorreo() != null && !usuario.getCorreo().isBlank()) {
            eventPublisher.publishEvent(new CorreoRecargo(usuario.getCorreo(), titulo, mensaje));
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarCorreoTrasConfirmarPago(CorreoRecargo aviso) {
        emailService.enviarAvisoRecargo(aviso.destinatario(), aviso.titulo(), aviso.mensaje());
    }

    public record CorreoRecargo(String destinatario, String titulo, String mensaje) { }
}
