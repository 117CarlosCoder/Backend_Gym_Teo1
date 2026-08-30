package com.example.backendgymteo1.service.profile;

import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileManagerService {

    private final List<RoleProfileStrategy> strategies;

    @Transactional
    public void registrarPerfilSegunRol(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        String roleName = (user.getRol() != null && user.getRol().getNombre() != null)
                ? user.getRol().getNombre().toUpperCase()
                : Rol.CLIENTE;

        strategies.stream()
                .filter(strategy -> strategy.soporta(roleName))
                .findFirst()
                .ifPresentOrElse(
                        strategy -> strategy.crearPerfil(user),
                        () -> log.info("[UserProfileManager] Rol {} no requiere tabla de perfil subordinada (ej. ADMIN)", roleName)
                );
    }
}
