package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.asistencia.AsistenciaDetalleDto;
import com.example.backendgymteo1.dto.asistencia.RegistrarAsistenciaDto;
import com.example.backendgymteo1.dto.asistencia.RegistroAsistenciaResponseDto;
import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.entity.Asistencia;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.Recepcionista;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.Sucursal;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.repository.AsistenciaRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.RecepcionistaRepository;
import com.example.backendgymteo1.repository.SocioRepository;
import com.example.backendgymteo1.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final SocioRepository socioRepository;
    private final MembresiaRepository membresiaRepository;
    private final SucursalRepository sucursalRepository;
    private final RecepcionistaRepository recepcionistaRepository;
    private final SocioService socioService;
    private final AuditoriaService auditoriaService;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    @Transactional(rollbackFor = Exception.class)
    public RegistroAsistenciaResponseDto registrarEntrada(RegistrarAsistenciaDto request, User usuarioAutenticado) {
        if (request == null) {
            throw new RuntimeException("Los datos de la solicitud son obligatorios");
        }
        return registrarEntrada(request.getIdSocio(), request.getSucursalId(), usuarioAutenticado);
    }

    @Transactional(rollbackFor = Exception.class)
    public RegistroAsistenciaResponseDto registrarEntrada(Integer idSocio, Integer sucursalId, User usuarioAutenticado) {
        if (idSocio == null) {
            throw new RuntimeException("El ID del socio es obligatorio");
        }
        if (sucursalId == null) {
            throw new RuntimeException("El ID de la sucursal es obligatorio");
        }
        if (usuarioAutenticado == null) {
            throw new RuntimeException("Se requiere un usuario autenticado para registrar la asistencia");
        }

        // 1. validar la existencia del socio
        Socio socio = socioRepository.findById(idSocio)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con ID: " + idSocio));

        if (socio.getUsuario() == null || !socio.getUsuario().isEnabled()) {
            throw new RuntimeException("El socio con ID " + idSocio + " se encuentra inactivo o dado de baja");
        }

        // 2.validar la membresia activa
        LocalDate hoy = LocalDate.now();
        Membresia membresiaActiva = membresiaRepository.findActiveBySocioId(socio.getId(), hoy)
                .orElseThrow(() -> new RuntimeException("El socio ID " + idSocio + " no cuenta con una membresía activa y vigente"));

        // 2.1 un socio no puede entrar dos veces sin registrar su salida
        List<Asistencia> abiertas = asistenciaRepository.findAbiertasBySocioIdAndFecha(socio.getId(), hoy);
        if (!abiertas.isEmpty()) {
            throw new RuntimeException(String.format(
                    "El socio ya se encuentra dentro del gimnasio desde las %s. Registre su salida antes de una nueva entrada.",
                    abiertas.get(0).getHoraEntrada().format(FORMATO_HORA)));
        }

        // 3. validar la sucurrsal
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + sucursalId));

        if (!sucursal.isActivo()) {
            throw new RuntimeException("La sucursal '" + sucursal.getNombre() + "' no se encuentra activa");
        }

        if (membresiaActiva.getSucursales() != null && !membresiaActiva.getSucursales().isEmpty()) {
            boolean tieneAcceso = membresiaActiva.getSucursales().stream()
                    .anyMatch(s -> s.getId().equals(sucursal.getId()));
            if (!tieneAcceso) {
                throw new RuntimeException(String.format(
                        "La membresía activa del socio no autoriza el acceso a la sucursal '%s'",
                        sucursal.getNombre()));
            }
        }

        // 4.obtener recepcionista (si el usuario autenticado aun no tiene perfil, se crea)
        Recepcionista recepcionista = recepcionistaRepository.findById(usuarioAutenticado.getId())
                .orElseGet(() -> recepcionistaRepository.save(Recepcionista.builder()
                        .usuario(usuarioAutenticado)
                        .fechaContratacion(LocalDate.now())
                        .build()));

        // 5. registro
        LocalTime horaEntrada = LocalTime.now();
        Asistencia asistencia = Asistencia.builder()
                .socio(socio)
                .recepcionista(recepcionista)
                .sucursal(sucursal)
                .fecha(hoy)
                .horaEntrada(horaEntrada)
                .build();

        asistencia = asistenciaRepository.save(asistencia);

        // 6. auditoria
        String nombreCompletoSocio = socio.getUsuario().getNombres() + " " + socio.getUsuario().getApellidos();
        auditoriaService.registrar(
                usuarioAutenticado,
                "asistencia",
                "INSERT",
                asistencia.getId(),
                String.format("Ingreso registrado para socio ID %d (%s) en sucursal '%s' por recepcionista ID %d",
                        socio.getId(), nombreCompletoSocio, sucursal.getNombre(), recepcionista.getId())
        );

        // 7. retornar confirmacion
        SocioResponseDto socioDto = socioService.findById(socio.getId());

        String nombreRecepcionista = (recepcionista.getUsuario() != null)
                ? recepcionista.getUsuario().getNombres() + " " + recepcionista.getUsuario().getApellidos()
                : (usuarioAutenticado.getNombres() != null ? usuarioAutenticado.getNombres() + " " + usuarioAutenticado.getApellidos() : "Recepcionista");

        return RegistroAsistenciaResponseDto.builder()
                .mensaje("Asistencia registrada exitosamente")
                .idAsistencia(asistencia.getId())
                .fecha(asistencia.getFecha())
                .horaEntrada(asistencia.getHoraEntrada())
                .recepcionistaId(recepcionista.getId())
                .recepcionistaNombre(nombreRecepcionista)
                .sucursalId(sucursal.getId())
                .sucursalNombre(sucursal.getNombre())
                .socio(socioDto)
                .build();
    }

    /**
     * Registra la hora de salida de una asistencia abierta.
     * Si la entrada es de un día anterior, la salida se cierra al final de ese día.
     */
    @Transactional(rollbackFor = Exception.class)
    public AsistenciaDetalleDto registrarSalida(Integer idAsistencia, User usuarioAutenticado) {
        Asistencia asistencia = asistenciaRepository.findByIdConDetalles(idAsistencia)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada con ID: " + idAsistencia));

        if (asistencia.getHoraSalida() != null) {
            throw new RuntimeException(String.format(
                    "La salida de esta asistencia ya fue registrada a las %s",
                    asistencia.getHoraSalida().format(FORMATO_HORA)));
        }

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        LocalTime horaSalida = asistencia.getFecha().isBefore(hoy) || ahora.isBefore(asistencia.getHoraEntrada())
                ? LocalTime.of(23, 59, 59)
                : ahora;
        asistencia.setHoraSalida(horaSalida);
        asistencia = asistenciaRepository.save(asistencia);

        if (usuarioAutenticado != null) {
            auditoriaService.registrar(
                    usuarioAutenticado,
                    "asistencia",
                    "UPDATE",
                    asistencia.getId(),
                    String.format("Salida registrada para socio ID %d a las %s",
                            asistencia.getSocio().getId(), horaSalida.format(FORMATO_HORA)));
        }
        return toDetalle(asistencia);
    }

    /** Asistencias de una fecha (por defecto hoy), opcionalmente filtradas por sucursal. */
    public List<AsistenciaDetalleDto> listarAsistenciasDelDia(LocalDate fecha, Integer sucursalId) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return asistenciaRepository.findUltimasAsistenciasConDetalles(sucursalId, dia, PageRequest.of(0, 500))
                .stream()
                .map(this::toDetalle)
                .toList();
    }

    /** Historial de entradas y salidas del socio autenticado (más recientes primero). */
    public List<AsistenciaDetalleDto> misAsistencias(User usuarioAutenticado, int limite) {
        if (usuarioAutenticado == null) {
            throw new RuntimeException("Se requiere un usuario autenticado");
        }
        Socio socio = socioRepository.findById(usuarioAutenticado.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario autenticado no tiene un perfil de socio"));
        return asistenciaRepository.findBySocioIdOrderByFechaDescHoraEntradaDesc(socio.getId())
                .stream()
                .limit(Math.max(1, limite))
                .map(this::toDetalle)
                .toList();
    }

    private AsistenciaDetalleDto toDetalle(Asistencia a) {
        Socio socio = a.getSocio();
        User usuarioSocio = socio != null ? socio.getUsuario() : null;
        Long minutos = a.getHoraSalida() != null
                ? Duration.between(a.getHoraEntrada(), a.getHoraSalida()).toMinutes()
                : null;

        String registradoPor = null;
        if (a.getUsuarioRegistro() != null) {
            registradoPor = a.getUsuarioRegistro().getNombres() + " " + a.getUsuarioRegistro().getApellidos();
        } else if (a.getRecepcionista() != null && a.getRecepcionista().getUsuario() != null) {
            registradoPor = a.getRecepcionista().getUsuario().getNombres() + " "
                    + a.getRecepcionista().getUsuario().getApellidos();
        }

        return AsistenciaDetalleDto.builder()
                .idAsistencia(a.getId())
                .idSocio(socio != null ? socio.getId() : null)
                .nombresSocio(usuarioSocio != null ? usuarioSocio.getNombres() : null)
                .apellidosSocio(usuarioSocio != null ? usuarioSocio.getApellidos() : null)
                .fecha(a.getFecha())
                .horaEntrada(a.getHoraEntrada())
                .horaSalida(a.getHoraSalida())
                .dentro(a.getHoraSalida() == null)
                .minutosPermanencia(minutos)
                .sucursalId(a.getSucursal() != null ? a.getSucursal().getId() : null)
                .sucursalNombre(a.getSucursal() != null ? a.getSucursal().getNombre() : null)
                .registradoPor(registradoPor)
                .build();
    }

    public Asistencia findById(Integer id) {
        return asistenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada con ID: " + id));
    }

    public List<Asistencia> findBySocioId(Integer socioId) {
        if (!socioRepository.existsById(socioId)) {
            throw new ResourceNotFoundException("Socio no encontrado con ID: " + socioId);
        }
        return asistenciaRepository.findBySocioIdOrderByFechaDescHoraEntradaDesc(socioId);
    }
}
