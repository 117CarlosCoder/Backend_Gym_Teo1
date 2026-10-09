package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.user.CreateUserDto;
import com.example.backendgymteo1.dto.user.UpdateProfileDto;
import com.example.backendgymteo1.dto.user.UpdateUserAdminDto;
import com.example.backendgymteo1.dto.user.UpdateUserEstadoDto;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    public List<UserResponseDto> findAll(boolean incluirEliminados) {
        List<User> users = incluirEliminados
                ? userRepository.findAll()
                : userRepository.findByEliminadoEnIsNull();

        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    public List<UserResponseDto> findAll() {
        return findAll(false);
    }

    public UserResponseDto findById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return userMapper.toDto(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponseDto updateEstado(Integer id, UpdateUserEstadoDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (user.getEliminadoEn() != null) {
            throw new RuntimeException("No se puede modificar el estado de una cuenta que ha sido eliminada");
        }

        user.setEstado(request.getEstado());
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void remove(Integer id) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        int updated = userRepository.softDeleteUser(id, now);
        if (updated == 0) {
            if (!userRepository.existsById(id)) {
                throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponseDto updateByAdmin(Integer id, UpdateUserAdminDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (user.getEliminadoEn() != null) {
            throw new RuntimeException("No se puede actualizar una cuenta que ha sido eliminada");
        }

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
        User user = userRepository.findByIdAndEliminadoEnIsNull(userId)
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
