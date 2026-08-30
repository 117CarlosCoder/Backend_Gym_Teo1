package com.example.backendgymteo1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private final RestClient restClient;
    private final String apiKey;
    private final String fromEmail;

    public EmailService(
            @Value("${resend.api-key:${RESEND_API_KEY:}}") String apiKey,
            @Value("${resend.from:${RESEND_FROM:Gimnasio <onboarding@resend.dev>}}") String fromEmail) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void enviarCorreo(String destinatario, String asunto, String htmlContent) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Resend API Key no configurada. No se envió el correo a: {}", destinatario);
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "from", fromEmail,
                    "to", List.of(destinatario),
                    "subject", asunto,
                    "html", htmlContent
            );

            Map<?, ?> response = restClient.post()
                    .uri("/emails")
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            log.info("Correo enviado exitosamente vía Resend a {} (ID: {})",
                    destinatario, response != null ? response.get("id") : "OK");
        } catch (Exception e) {
            log.error("No se pudo enviar el correo vía Resend a {}: {}", destinatario, e.getMessage());
        }
    }

    public void enviarCredenciales(String destinatario, String nombreCompleto, String contraseniaTemporal, String rol) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Resend API Key no configurada. Credenciales generadas para {} (Rol: {}): [{}]",
                    destinatario, rol, contraseniaTemporal);
            return;
        }

        String htmlContent = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                <h2 style="color: #2c3e50; text-align: center;">¡Bienvenido al Gimnasio!</h2>
                <p style="font-size: 16px; color: #333;">Hola <strong>%s</strong>,</p>
                <p style="font-size: 14px; color: #555;">Has sido registrado en nuestro sistema con el rol: <strong style="color: #2980b9;">%s</strong>.</p>
                <div style="background-color: #f8f9fa; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #3498db;">
                    <p style="margin: 5px 0; font-size: 14px;"><strong>Correo de acceso:</strong> %s</p>
                    <p style="margin: 5px 0; font-size: 14px;"><strong>Contraseña temporal:</strong> <code style="background: #e2e8f0; padding: 3px 6px; border-radius: 4px; font-size: 15px; font-weight: bold;">%s</code></p>
                </div>
                <p style="font-size: 13px; color: #7f8c8d;">Por favor, inicia sesión en la plataforma y actualiza tu contraseña desde tu perfil por motivos de seguridad.</p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;" />
                <p style="font-size: 12px; color: #95a5a6; text-align: center;">Administración del Gimnasio &copy; 2026</p>
            </div>
            """, nombreCompleto, rol, destinatario, contraseniaTemporal);

        enviarCorreo(destinatario, "Bienvenido al Gimnasio - Tus credenciales de acceso", htmlContent);
    }
}
