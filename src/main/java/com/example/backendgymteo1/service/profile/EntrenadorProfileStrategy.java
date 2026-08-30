package com.example.backendgymteo1.service.profile;

import com.example.backendgymteo1.entity.Entrenador;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.EntrenadorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EntrenadorProfileStrategy implements RoleProfileStrategy {

    private final EntrenadorRepository entrenadorRepository;

    @Override
    public boolean soporta(String nombreRol) {
        return Rol.ENTRENADOR.equalsIgnoreCase(nombreRol);
    }

    @Override
    public void crearPerfil(User user) {
        if (!entrenadorRepository.existsById(user.getId())) {
            entrenadorRepository.save(Entrenador.builder()
                    .id(user.getId())
                    .usuario(user)
                    .fechaContratacion(LocalDate.now())
                    .build());
            log.info("[Strategy] Perfil de entrenador creado para usuario ID {}", user.getId());
        }
    }
}
