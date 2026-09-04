package com.example.backendgymteo1.service.profile;

import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.Sucursal;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.SocioRepository;
import com.example.backendgymteo1.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocioProfileStrategy implements RoleProfileStrategy {

    private final SocioRepository socioRepository;
    private final SucursalRepository sucursalRepository;

    @Override
    public boolean soporta(String nombreRol) {
        return Rol.CLIENTE.equalsIgnoreCase(nombreRol);
    }

    @Override
    public void crearPerfil(User user) {
        if (!socioRepository.existsById(user.getId())) {
            Sucursal sucursalDefault = sucursalRepository.findById(1)
                    .orElseGet(() -> sucursalRepository.findAll().stream().findFirst().orElse(null));

            socioRepository.save(Socio.builder()
                    .id(user.getId())
                    .usuario(user)
                    .sucursal(sucursalDefault)
                    .fechaRegistro(LocalDate.now())
                    .build());
            log.info("[Strategy] Perfil de socio creado para usuario ID {} (Sucursal: {})",
                    user.getId(), sucursalDefault != null ? sucursalDefault.getNombre() : "N/A");
        }
    }
}
