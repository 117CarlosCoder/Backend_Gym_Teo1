package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.user.CreateUserDto;
import com.example.backendgymteo1.dto.user.UpdateProfileDto;
import com.example.backendgymteo1.dto.user.UpdateUserAdminDto;
import com.example.backendgymteo1.dto.user.UserResponseDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.mapper.UserMapper;
import com.example.backendgymteo1.repository.UserRepository;
import com.example.backendgymteo1.service.profile.UserProfileManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileManagerService userProfileManagerService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final PasswordGeneratorService passwordGeneratorService;

    @Transactional(rollbackFor = Exception.class)
    public UserResponseDto create(CreateUserDto request) {
        if (userRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        if (userRepository.existsByDpi(request.getDpi())) {
            throw new RuntimeException("El DPI ya está registrado");
        }

        String passwordPlana = passwordGeneratorService.generarContraseniaAleatoria();

        String encodedPassword = passwordEncoder.encode(passwordPlana);
        User user = userMapper.toEntity(request, encodedPassword);

        user = userRepository.save(user);
        userProfileManagerService.registrarPerfilSegunRol(user);

        String nombreCompleto = user.getNombres() + " " + user.getApellidos();
        emailService.enviarCredenciales(user.getCorreo(), nombreCompleto, passwordPlana, user.getRol().name());

        return userMapper.toDto(user, passwordPlana);
    }

    public List<UserResponseDto> findAll() {
        return userRepository.findByEstadoTrue()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserResponseDto findById(Integer id) {
        User user = userRepository.findByIdAndEstadoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return userMapper.toDto(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Integer id) {
        User user = userRepository.findByIdAndEstadoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        user.setEstado(false);
        user.setEliminadoEn(LocalDateTime.now());

        user.setCorreo("del_" + user.getId() + "_" + user.getCorreo());
        user.setDpi("del_" + user.getId() + "_" + user.getDpi());
        if (user.getTelefono() != null && !user.getTelefono().isBlank()) {
            user.setTelefono("del_" + user.getId() + "_" + user.getTelefono());
        }

        userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponseDto updateByAdmin(Integer id, UpdateUserAdminDto request) {
        User user = userRepository.findByIdAndEstadoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (request.getDpi() != null && !request.getDpi().isBlank() && !request.getDpi().equals(user.getDpi())) {
            if (userRepository.existsByDpi(request.getDpi())) {
                throw new RuntimeException("El DPI ya está registrado por otro usuario");
            }
        }

        if (request.getCorreo() != null && !request.getCorreo().isBlank()
                && !request.getCorreo().equalsIgnoreCase(user.getCorreo())) {
            if (userRepository.existsByCorreo(request.getCorreo())) {
                throw new RuntimeException("El correo ya está registrado por otro usuario");
            }
        }

        String encodedPassword = null;
        if (request.getContrasenia() != null && !request.getContrasenia().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getContrasenia());
        }

        userMapper.updateEntityFromAdmin(user, request, encodedPassword);
        user = userRepository.save(user);

        if (request.getRol() != null) {
            userProfileManagerService.registrarPerfilSegunRol(user);
        }

        return userMapper.toDto(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponseDto updateProfile(Integer userId, UpdateProfileDto request) {
        User user = userRepository.findByIdAndEstadoTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        if (request.getCorreo() != null && !request.getCorreo().isBlank()
                && !request.getCorreo().equalsIgnoreCase(user.getCorreo())) {
            if (userRepository.existsByCorreo(request.getCorreo())) {
                throw new RuntimeException("El correo ya está registrado por otro usuario");
            }
        }

        String encodedPassword = null;
        if (request.getNuevaContrasenia() != null && !request.getNuevaContrasenia().isBlank()) {
            if (request.getContraseniaActual() == null || request.getContraseniaActual().isBlank()) {
                throw new RuntimeException("Debes ingresar tu contraseña actual para cambiarla");
            }
            if (!passwordEncoder.matches(request.getContraseniaActual(), user.getPassword())) {
                throw new RuntimeException("La contraseña actual es incorrecta");
            }
            encodedPassword = passwordEncoder.encode(request.getNuevaContrasenia());
        }

        userMapper.updateEntityFromProfile(user, request, encodedPassword);
        user = userRepository.save(user);

        return userMapper.toDto(user);
    }
}
