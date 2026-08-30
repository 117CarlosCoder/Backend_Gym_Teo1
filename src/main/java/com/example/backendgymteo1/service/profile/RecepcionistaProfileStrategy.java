package com.example.backendgymteo1.service.profile;

import com.example.backendgymteo1.entity.Recepcionista;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.RecepcionistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecepcionistaProfileStrategy implements RoleProfileStrategy {

    private final RecepcionistaRepository recepcionistaRepository;

    @Override
    public boolean soporta(String nombreRol) {
        return Rol.RECEPCIONISTA.equalsIgnoreCase(nombreRol);
    }

    @Override
    public void crearPerfil(User user) {
        if (!recepcionistaRepository.existsById(user.getId())) {
            recepcionistaRepository.save(Recepcionista.builder()
                    .id(user.getId())
                    .usuario(user)
                    .fechaContratacion(LocalDate.now())
                    .build());
            log.info("[Strategy] Perfil de recepcionista creado para usuario ID {}", user.getId());
        }
    }
}
