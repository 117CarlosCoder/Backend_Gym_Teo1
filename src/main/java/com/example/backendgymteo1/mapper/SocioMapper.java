package com.example.backendgymteo1.mapper;

import com.example.backendgymteo1.dto.socio.CreateSocioDto;
import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.dto.socio.UpdateSocioDto;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SocioMapper {

    private final UserMapper userMapper;

    public SocioResponseDto toDto(Socio socio) {
        if (socio == null) {
            return null;
        }
        return SocioResponseDto.builder()
                .id(socio.getId())
                .fechaRegistro(socio.getFechaRegistro())
                .usuario(userMapper.toDto(socio.getUsuario()))
                .build();
    }

    public SocioResponseDto toDto(Socio socio, String temporaryPassword) {
        if (socio == null) {
            return null;
        }
        return SocioResponseDto.builder()
                .id(socio.getId())
                .fechaRegistro(socio.getFechaRegistro())
                .usuario(userMapper.toDto(socio.getUsuario(), temporaryPassword))
                .build();
    }

    public User toUserEntity(CreateSocioDto request, String encodedPassword) {
        return User.builder()
                .dpi(request.getDpi())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .contrasenia(encodedPassword)
                .rol(Rol.cliente())
                .estado(true)
                .build();
    }

    public Socio toSocioEntity(User user, LocalDate fechaRegistro) {
        return Socio.builder()
                .id(user.getId())
                .usuario(user)
                .fechaRegistro(fechaRegistro != null ? fechaRegistro : LocalDate.now())
                .build();
    }

    public void updateEntity(Socio socio, UpdateSocioDto request) {
        if (request.getFechaRegistro() != null) {
            socio.setFechaRegistro(request.getFechaRegistro());
        }

        User user = socio.getUsuario();
        if (user != null) {
            if (request.getDpi() != null && !request.getDpi().isBlank()) {
                user.setDpi(request.getDpi());
            }
            if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
                user.setCorreo(request.getCorreo());
            }
            if (request.getNombres() != null && !request.getNombres().isBlank()) {
                user.setNombres(request.getNombres());
            }
            if (request.getApellidos() != null && !request.getApellidos().isBlank()) {
                user.setApellidos(request.getApellidos());
            }
            if (request.getTelefono() != null) {
                user.setTelefono(request.getTelefono());
            }
            if (request.getEstado() != null) {
                user.setEstado(request.getEstado());
            }
        }
    }
}
