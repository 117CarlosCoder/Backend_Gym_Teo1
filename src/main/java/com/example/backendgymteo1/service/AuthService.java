package com.example.backendgymteo1.service;

import com.example.backendgymteo1.config.security.JwtService;
import com.example.backendgymteo1.dto.auth.AuthResponseDto;
import com.example.backendgymteo1.dto.auth.LoginRequestDto;
import com.example.backendgymteo1.dto.auth.RegisterRequestDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.mapper.AuthMapper;
import com.example.backendgymteo1.mapper.UserMapper;
import com.example.backendgymteo1.repository.UserRepository;
import com.example.backendgymteo1.service.profile.UserProfileManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileManagerService userProfileManagerService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;

    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getCorreo(),
                        request.getContrasenia()));

        User user = userRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con correo: " + request.getCorreo()));

        String jwtToken = jwtService.generarToken(user);
        return authMapper.toResponseDto(user, jwtToken);
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        if (userRepository.existsByDpi(request.getDpi())) {
            throw new RuntimeException("El DPI ya está registrado");
        }

        String encodedPassword = passwordEncoder.encode(request.getContrasenia());
        User user = userMapper.toEntity(request, encodedPassword);

        user = userRepository.save(user);
        userProfileManagerService.registrarPerfilSegunRol(user);

        String jwtToken = jwtService.generarToken(user);
        return authMapper.toResponseDto(user, jwtToken);
    }
}
