package com.example.backendgymteo1.service;

import com.example.backendgymteo1.entity.Bitacora;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.BitacoraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final BitacoraRepository bitacoraRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void registrar(User usuario, String nombreTabla, String accion, Integer idRegistro, String descripcion) {
        try {
            Bitacora bitacora = Bitacora.builder()
                    .usuario(usuario)
                    .nombreTabla(nombreTabla)
                    .accion(accion)
                    .idRegistro(idRegistro)
                    .fechaHora(LocalDateTime.now())
                    .descripcion(descripcion)
                    .build();

            bitacoraRepository.save(bitacora);
            log.info("[AUDITORIA] Usuario '{}' realizó '{}' sobre tabla '{}' (ID: {}). Detalle: {}",
                    usuario != null ? usuario.getCorreo() : "SISTEMA",
                    accion,
                    nombreTabla,
                    idRegistro,
                    descripcion);
        } catch (Exception e) {
            log.error("[AUDITORIA] Error al registrar evento en bitácora: {}", e.getMessage(), e);
        }
    }
}
