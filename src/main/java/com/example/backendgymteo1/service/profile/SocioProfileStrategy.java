package com.example.backendgymteo1.service.profile;

import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocioProfileStrategy implements RoleProfileStrategy {

    private final SocioRepository socioRepository;

    @Override
    public boolean soporta(String nombreRol) {
        return Rol.CLIENTE.equalsIgnoreCase(nombreRol);
    }

    @Override
    public void crearPerfil(User user) {
        if (!socioRepository.existsById(user.getId())) {
            socioRepository.save(Socio.builder()
                    .id(user.getId())
                    .usuario(user)
                    .fechaRegistro(LocalDate.now())
                    .build());
            log.info("[Strategy] Perfil de socio creado para usuario ID {}", user.getId());
        }
    }
}
