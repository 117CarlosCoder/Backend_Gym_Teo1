package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.socio.CreateSocioDto;
import com.example.backendgymteo1.dto.socio.MembresiaResumenDto;
import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.dto.socio.UltimaAsistenciaDto;
import com.example.backendgymteo1.dto.socio.UpdateSocioDto;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.Sucursal;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.mapper.MembresiaMapper;
import com.example.backendgymteo1.mapper.SocioMapper;
import com.example.backendgymteo1.repository.AsistenciaRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.SocioRepository;
import com.example.backendgymteo1.repository.SucursalRepository;
import com.example.backendgymteo1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocioService {

    private final SocioRepository socioRepository;
    private final UserRepository userRepository;
    private final SucursalRepository sucursalRepository;
    private final MembresiaRepository membresiaRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SocioMapper socioMapper;
    private final MembresiaMapper membresiaMapper;
    private final PasswordGeneratorService passwordGeneratorService;
    private final AuditoriaService auditoriaService;

    @Transactional(rollbackFor = Exception.class)
    public SocioResponseDto create(CreateSocioDto request) {
        return create(request, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public SocioResponseDto create(CreateSocioDto request, User usuarioActual) {
        if (userRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        if (userRepository.existsByDpi(request.getDpi())) {
            throw new RuntimeException("El DPI ya está registrado");
        }

        Sucursal sucursal;
        if (request.getSucursalId() != null) {
            sucursal = sucursalRepository.findById(request.getSucursalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + request.getSucursalId()));
        } else {
            sucursal = sucursalRepository.findById(1)
                    .orElseGet(() -> sucursalRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("No existen sucursales registradas en el sistema")));
        }

        String passwordPlana = passwordGeneratorService.generarContraseniaAleatoria();
        String encodedPassword = passwordEncoder.encode(passwordPlana);

        User user = socioMapper.toUserEntity(request, encodedPassword);
        user = userRepository.saveAndFlush(user);

        Socio socio = socioMapper.toSocioEntity(user, sucursal, request.getFechaRegistro());
        socio = socioRepository.save(socio);

        String nombreCompleto = user.getNombres() + " " + user.getApellidos();
        emailService.enviarCredenciales(user.getCorreo(), nombreCompleto, passwordPlana, user.getRol().name());

        auditoriaService.registrar(
                usuarioActual,
                "socio",
                "INSERT",
                socio.getId(),
                String.format("Socio creado con ID %d, DPI: %s, Correo: %s, Sucursal: %s",
                        socio.getId(), user.getDpi(), user.getCorreo(), sucursal.getNombre())
        );

        return socioMapper.toDto(socio, passwordPlana);
    }

    public Page<SocioResponseDto> findAll(Pageable pageable) {
        return socioRepository.findAllActiveWithUser(pageable)
                .map(socioMapper::toDto);
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

        MembresiaResumenDto membresiaResumen = membresiaRepository.findActiveBySocioId(id, LocalDate.now())
                .map(membresiaMapper::toResumenDto)
                .orElse(null);

        UltimaAsistenciaDto ultimaAsistencia = asistenciaRepository.findUltimaAsistenciaBySocioId(id)
                .map(a -> UltimaAsistenciaDto.builder()
                        .idAsistencia(a.getId())
                        .fecha(a.getFecha())
                        .horaEntrada(a.getHoraEntrada())
                        .horaSalida(a.getHoraSalida())
                        .recepcionista(a.getRecepcionista() != null && a.getRecepcionista().getUsuario() != null
                                ? a.getRecepcionista().getUsuario().getNombres() + " " + a.getRecepcionista().getUsuario().getApellidos()
                                : null)
                        .sucursal(a.getSucursal() != null ? a.getSucursal().getNombre() : null)
                        .build())
                .orElse(null);

        return socioMapper.toDto(socio, null, membresiaResumen, ultimaAsistencia);
    }

    @Transactional(rollbackFor = Exception.class)
    public SocioResponseDto update(Integer id, UpdateSocioDto request) {
        return update(id, request, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public SocioResponseDto update(Integer id, UpdateSocioDto request, User usuarioActual) {
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

        List<String> cambios = new ArrayList<>();
        if (request.getNombres() != null && !Objects.equals(request.getNombres(), user.getNombres())) {
            cambios.add(String.format("nombres: '%s' -> '%s'", user.getNombres(), request.getNombres()));
        }
        if (request.getApellidos() != null && !Objects.equals(request.getApellidos(), user.getApellidos())) {
            cambios.add(String.format("apellidos: '%s' -> '%s'", user.getApellidos(), request.getApellidos()));
        }
        if (request.getCorreo() != null && !Objects.equals(request.getCorreo(), user.getCorreo())) {
            cambios.add(String.format("correo: '%s' -> '%s'", user.getCorreo(), request.getCorreo()));
        }
        if (request.getTelefono() != null && !Objects.equals(request.getTelefono(), user.getTelefono())) {
            cambios.add(String.format("telefono: '%s' -> '%s'", user.getTelefono(), request.getTelefono()));
        }
        if (request.getDireccion() != null && !Objects.equals(request.getDireccion(), user.getDireccion())) {
            cambios.add(String.format("direccion: '%s' -> '%s'", user.getDireccion(), request.getDireccion()));
        }
        if (request.getFechaNacimiento() != null && !Objects.equals(request.getFechaNacimiento(), user.getFechaNacimiento())) {
            cambios.add(String.format("fechaNacimiento: '%s' -> '%s'", user.getFechaNacimiento(), request.getFechaNacimiento()));
        }
        if (request.getSucursalId() != null && (socio.getSucursal() == null || !Objects.equals(request.getSucursalId(), socio.getSucursal().getId()))) {
            Sucursal nuevaSucursal = sucursalRepository.findById(request.getSucursalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + request.getSucursalId()));
            cambios.add(String.format("sucursal: '%s' -> '%s'", socio.getSucursal() != null ? socio.getSucursal().getNombre() : "N/A", nuevaSucursal.getNombre()));
            socio.setSucursal(nuevaSucursal);
        }

        socioMapper.updateEntity(socio, request);
        userRepository.save(user);
        socio = socioRepository.save(socio);

        String descripcionCambios = cambios.isEmpty() ? "Actualización de socio sin cambios detectados" : String.join(", ", cambios);
        auditoriaService.registrar(usuarioActual, "socio", "UPDATE", socio.getId(), descripcionCambios);

        return findById(socio.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Integer id) {
        remove(id, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Integer id, User usuarioActual) {
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

        auditoriaService.registrar(
                usuarioActual,
                "socio",
                "DELETE",
                socio.getId(),
                "Baja lógica (soft delete) del socio con ID: " + socio.getId()
        );
    }
}
