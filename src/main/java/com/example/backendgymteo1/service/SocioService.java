package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.socio.CreateSocioDto;
import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.dto.socio.UpdateSocioDto;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.mapper.SocioMapper;
import com.example.backendgymteo1.repository.SocioRepository;
import com.example.backendgymteo1.repository.UserRepository;
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
public class SocioService {

    private final SocioRepository socioRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SocioMapper socioMapper;
    private final PasswordGeneratorService passwordGeneratorService;

    @Transactional(rollbackFor = Exception.class)
    public SocioResponseDto create(CreateSocioDto request) {
        if (userRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        if (userRepository.existsByDpi(request.getDpi())) {
            throw new RuntimeException("El DPI ya está registrado");
        }

        String passwordPlana = passwordGeneratorService.generarContraseniaAleatoria();
        String encodedPassword = passwordEncoder.encode(passwordPlana);

        User user = socioMapper.toUserEntity(request, encodedPassword);
        user = userRepository.save(user);

        Socio socio = socioMapper.toSocioEntity(user, request.getFechaRegistro());
        socio = socioRepository.save(socio);

        String nombreCompleto = user.getNombres() + " " + user.getApellidos();
        emailService.enviarCredenciales(user.getCorreo(), nombreCompleto, passwordPlana, user.getRol().name());

        return socioMapper.toDto(socio, passwordPlana);
    }

    public List<SocioResponseDto> findAll() {
        return socioRepository.findAllActiveWithUser()
                .stream()
                .map(socioMapper::toDto)
                .toList();
    }

    public SocioResponseDto findById(Integer id) {
        Socio socio = socioRepository.findByIdAndActiveWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con ID: " + id));
        return socioMapper.toDto(socio);
    }

    @Transactional(rollbackFor = Exception.class)
    public SocioResponseDto update(Integer id, UpdateSocioDto request) {
        Socio socio = socioRepository.findByIdAndActiveWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con ID: " + id));

        User user = socio.getUsuario();

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

        socioMapper.updateEntity(socio, request);
        userRepository.save(user);
        socio = socioRepository.save(socio);

        return socioMapper.toDto(socio);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Integer id) {
        Socio socio = socioRepository.findByIdAndActiveWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con ID: " + id));

        User user = socio.getUsuario();

        user.setEstado(false);
        user.setEliminadoEn(LocalDateTime.now());

        user.setCorreo("del_" + user.getId() + "_" + user.getCorreo());
        user.setDpi("del_" + user.getId() + "_" + user.getDpi());
        if (user.getTelefono() != null && !user.getTelefono().isBlank()) {
            user.setTelefono("del_" + user.getId() + "_" + user.getTelefono());
        }

        userRepository.save(user);
    }
}
