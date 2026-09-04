package com.example.backendgymteo1.config.security;

import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.MembresiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component("membresiaSecurity")
@RequiredArgsConstructor
public class MembresiaSecurityService {

    private final MembresiaRepository membresiaRepository;

    public boolean tieneMembresiaActiva(Object principal) {
        if (!(principal instanceof User user)) {
            return false;
        }

        if (!user.isEnabled()) {
            log.warn("[Security] Acceso denegado: Usuario ID {} está deshabilitado o eliminado por soft delete",
                    user.getId());
            return false;
        }

        boolean esPersonal = user.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) ||
                "ROLE_RECEPCIONISTA".equals(a.getAuthority()) ||
                "ROLE_ENTRENADOR".equals(a.getAuthority()));

        if (esPersonal) {
            return true;
        }

        boolean tieneActiva = membresiaRepository.findActiveBySocioId(user.getId(), LocalDate.now()).isPresent();
        if (!tieneActiva) {
            log.warn("[Security] Acceso denegado para usuario ID {}: Membresía no activa, vencida o inexistente",
                    user.getId());
        }
        return tieneActiva;
    }

    public boolean tieneMembresiaActivaPorSocioId(Integer socioId) {
        if (socioId == null) {
            return false;
        }
        return membresiaRepository.findActiveBySocioId(socioId, LocalDate.now()).isPresent();
    }
}
