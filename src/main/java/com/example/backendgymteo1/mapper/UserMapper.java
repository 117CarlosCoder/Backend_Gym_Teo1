package com.example.backendgymteo1.mapper;

import com.example.backendgymteo1.dto.auth.RegisterRequestDto;
import com.example.backendgymteo1.dto.user.CreateUserDto;
import com.example.backendgymteo1.dto.user.UpdateProfileDto;
import com.example.backendgymteo1.dto.user.UpdateUserAdminDto;
import com.example.backendgymteo1.dto.user.UserResponseDto;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(user.getId())
                .dpi(user.getDpi())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .telefono(user.getTelefono())
                .direccion(user.getDireccion())
                .fechaNacimiento(user.getFechaNacimiento())
                .correo(user.getCorreo())
                .rol(user.getRol())
                .estado(user.isEstado())
                .creadoEn(user.getCreadoEn())
                .actualizadoEn(user.getActualizadoEn())
                .eliminadoEn(user.getEliminadoEn())
                .build();
    }

    public UserResponseDto toDto(User user, String temporaryPassword) {
        UserResponseDto dto = toDto(user);
        if (dto != null) {
            dto.setContraseniaTemporal(temporaryPassword);
        }
        return dto;
    }

    public User toEntity(CreateUserDto request, String encodedPassword) {
        return User.builder()
                .dpi(request.getDpi())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .contrasenia(encodedPassword)
                .rol(request.getRol() != null ? request.getRol() : Rol.cliente())
                .estado(true)
                .build();
    }

    public User toEntity(RegisterRequestDto request, String encodedPassword) {
        return User.builder()
                .dpi(request.getDpi())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .contrasenia(encodedPassword)
                .rol(request.getRol() != null ? request.getRol() : Rol.cliente())
                .estado(true)
                .build();
    }

    public void updateEntityFromAdmin(User user, UpdateUserAdminDto request, String encodedPassword) {
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
        if (encodedPassword != null) {
            user.setContrasenia(encodedPassword);
        }
        if (request.getRol() != null) {
            user.setRol(request.getRol());
        }
        if (request.getEstado() != null) {
            user.setEstado(request.getEstado());
        }
    }

    public void updateEntityFromProfile(User user, UpdateProfileDto request, String encodedPassword) {
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
        if (encodedPassword != null) {
            user.setContrasenia(encodedPassword);
        }
    }
}
