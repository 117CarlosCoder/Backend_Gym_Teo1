package com.example.backendgymteo1.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {
    @Test
    void correoDeRecargoEscapaElContenidoYUsaElCorreoDelSocio() {
        var service = spy(new EmailService("", "Gimnasio <onboarding@resend.dev>"));
        doNothing().when(service).enviarCorreo(anyString(), anyString(), anyString());
        service.enviarAvisoRecargo("socio@example.com", "Recargo <plan>",
                "Descuento: Q50.00. Recargo: Q25.00. <script>alert(1)</script>");
        var html = ArgumentCaptor.forClass(String.class);
        verify(service).enviarCorreo(eq("socio@example.com"), eq("Recargo <plan> - Gimnasio"), html.capture());
        assertTrue(html.getValue().contains("Recargo &lt;plan&gt;"));
        assertTrue(html.getValue().contains("Descuento: Q50.00. Recargo: Q25.00."));
        assertFalse(html.getValue().contains("<script>"));
    }
}
