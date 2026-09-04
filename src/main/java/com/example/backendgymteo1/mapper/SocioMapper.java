package com.example.backendgymteo1.mapper;

import com.example.backendgymteo1.dto.socio.CreateSocioDto;
import com.example.backendgymteo1.dto.socio.MembresiaResumenDto;
import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.dto.socio.UltimaAsistenciaDto;
import com.example.backendgymteo1.dto.socio.UpdateSocioDto;
import com.example.backendgymteo1.dto.sucursal.SucursalResponseDto;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.Sucursal;
import com.example.backendgymteo1.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SocioMapper {

    private final UserMapper userMapper;

    public SocioResponseDto toDto(Socio socio) {
        return toDto(socio, null, null, null);
    }

    public SocioResponseDto toDto(Socio socio, String temporaryPassword) {
        return toDto(socio, temporaryPassword, null, null);
    }

    public SocioResponseDto toDto(
            Socio socio,
            String temporaryPassword,
            MembresiaResumenDto membresiaActual,
            UltimaAsistenciaDto ultimaAsistencia) {
        if (socio == null) {
            return null;
        }

        SucursalResponseDto sucursalDto = null;
        if (socio.getSucursal() != null) {
            sucursalDto = SucursalResponseDto.builder()
                    .id(socio.getSucursal().getId())
                    .nombre(socio.getSucursal().getNombre())
                    .direccion(socio.getSucursal().getDireccion())
                    .telefono(socio.getSucursal().getTelefono())
                    .activo(socio.getSucursal().isActivo())
                    .build();
        }

        Boolean estado = socio.getUsuario() != null ? socio.getUsuario().isEstado() : null;

        return SocioResponseDto.builder()
                .id(socio.getId())
                .fechaRegistro(socio.getFechaRegistro())
                .estado(estado)
                .usuario(temporaryPassword != null
                        ? userMapper.toDto(socio.getUsuario(), temporaryPassword)
                        : userMapper.toDto(socio.getUsuario()))
                .sucursal(sucursalDto)
                .membresiaActual(membresiaActual)
                .ultimaAsistencia(ultimaAsistencia)
                .build();
    }

    public User toUserEntity(CreateSocioDto request, String encodedPassword) {
        return User.builder()
                .dpi(request.getDpi())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .fechaNacimiento(request.getFechaNacimiento())
                .correo(request.getCorreo())
                .contrasenia(encodedPassword)
                .rol(Rol.cliente())
                .estado(true)
                .build();
    }

    public Socio toSocioEntity(User user, Sucursal sucursal, LocalDate fechaRegistro) {
        return Socio.builder()
                .id(user.getId())
                .usuario(user)
                .sucursal(sucursal)
                .fechaRegistro(fechaRegistro != null ? fechaRegistro : LocalDate.now())
                .build();
    }

    public void updateEntity(Socio socio, UpdateSocioDto request) {
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
            if (request.getDireccion() != null) {
                user.setDireccion(request.getDireccion());
            }
            if (request.getFechaNacimiento() != null) {
                user.setFechaNacimiento(request.getFechaNacimiento());
            }
            if (request.getEstado() != null) {
                user.setEstado(request.getEstado());
            }
        }
    }
}
