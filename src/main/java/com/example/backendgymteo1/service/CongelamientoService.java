package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.configuracion.ReglasCongelamientoDto;
import com.example.backendgymteo1.dto.membresia.CongelamientoResponseDto;
import com.example.backendgymteo1.dto.membresia.CreateCongelamientoDto;
import com.example.backendgymteo1.entity.CongelamientoMembresia;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.repository.CongelamientoMembresiaRepository;
import com.example.backendgymteo1.repository.EstadoMembresiaRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Ajuste del PO: congelar temporalmente una membresía sin que se consuma el tiempo congelado.
 * Reglas (configurables por el ADMIN): duración mínima y máxima, y cantidad por año.
 * Al registrar el congelamiento se suman sus días al vencimiento; mientras está en curso
 * la membresía queda CONGELADA y el socio no puede registrar su ingreso.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CongelamientoService {

    static final int ESTADO_ACTIVA = 1;
    static final int ESTADO_CONGELADA = 3;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CongelamientoMembresiaRepository congelamientoRepository;
    private final MembresiaRepository membresiaRepository;
    private final EstadoMembresiaRepository estadoMembresiaRepository;
    private final ConfiguracionSistemaService configuracionService;
    private final AuditoriaService auditoriaService;

    public List<CongelamientoResponseDto> listarPorMembresia(Integer idMembresia) {
        Membresia membresia = membresiaRepository.findByIdWithDetails(idMembresia)
                .orElseThrow(() -> new ResourceNotFoundException("Membresía no encontrada con ID: " + idMembresia));
        LocalDate hoy = LocalDate.now();
        return congelamientoRepository.findByMembresiaId(idMembresia).stream()
                .map(c -> toDto(c, membresia, hoy))
                .toList();
    }

    public List<CongelamientoResponseDto> listarPorSocio(Integer socioId) {
        LocalDate hoy = LocalDate.now();
        return congelamientoRepository.findBySocioId(socioId).stream()
                .map(c -> toDto(c, c.getMembresia(), hoy))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public CongelamientoResponseDto congelar(Integer idMembresia, CreateCongelamientoDto request, User usuarioActual) {
        if (request == null || request.getFechaFin() == null) {
            throw new RuntimeException("La fecha final del congelamiento es obligatoria");
        }
        Membresia membresia = membresiaRepository.findByIdWithDetails(idMembresia)
                .orElseThrow(() -> new ResourceNotFoundException("Membresía no encontrada con ID: " + idMembresia));

        LocalDate hoy = LocalDate.now();
        LocalDate inicio = request.getFechaInicio() != null ? request.getFechaInicio() : hoy;
        LocalDate fin = request.getFechaFin();

        validarMembresiaCongelable(membresia, hoy);

        if (inicio.isBefore(hoy)) {
            throw new RuntimeException("El congelamiento no puede iniciar en una fecha pasada");
        }
        if (fin.isBefore(inicio)) {
            throw new RuntimeException("La fecha final no puede ser anterior a la fecha de inicio");
        }
        if (inicio.isAfter(membresia.getFechaVencimiento())) {
            throw new RuntimeException(String.format(
                    "El congelamiento debe iniciar antes del vencimiento de la membresía (%s)",
                    membresia.getFechaVencimiento().format(FORMATO_FECHA)));
        }

        ReglasCongelamientoDto reglas = configuracionService.consultarReglasCongelamiento();
        int dias = (int) ChronoUnit.DAYS.between(inicio, fin) + 1;
        if (dias < reglas.getDiasMinimo() || dias > reglas.getDiasMaximo()) {
            throw new RuntimeException(String.format(
                    "El congelamiento debe durar entre %d y %d días (solicitado: %d)",
                    reglas.getDiasMinimo(), reglas.getDiasMaximo(), dias));
        }
        if (congelamientoRepository.existeTraslape(membresia.getId(), inicio, fin)) {
            throw new RuntimeException("Ya existe un congelamiento para esta membresía que se cruza con esas fechas");
        }
        long enElAnio = congelamientoRepository.contarEnAnio(membresia.getId(), inicio.getYear());
        if (enElAnio >= reglas.getMaximoPorAnio()) {
            throw new RuntimeException(String.format(
                    "La membresía ya alcanzó el máximo de %d congelamiento(s) permitidos en %d",
                    reglas.getMaximoPorAnio(), inicio.getYear()));
        }

        CongelamientoMembresia congelamiento = congelamientoRepository.save(CongelamientoMembresia.builder()
                .membresia(membresia)
                .fechaInicio(inicio)
                .fechaFin(fin)
                .dias(dias)
                .motivo(request.getMotivo() != null && !request.getMotivo().isBlank() ? request.getMotivo().trim() : null)
                .fechaRegistro(LocalDateTime.now())
                .registradoPor(nombreDe(usuarioActual))
                .build());

        // Extensión automática: el tiempo congelado no se consume
        LocalDate vencimientoAnterior = membresia.getFechaVencimiento();
        membresia.setFechaVencimiento(vencimientoAnterior.plusDays(dias));
        if (!inicio.isAfter(hoy)) {
            membresia.setEstadoMembresia(estado(ESTADO_CONGELADA, "CONGELADA"));
        }
        membresiaRepository.save(membresia);

        auditoriaService.registrar(usuarioActual, "congelamiento_membresia", "INSERT", congelamiento.getId(),
                String.format("Membresía ID %d congelada del %s al %s (%d días). Vencimiento %s -> %s",
                        membresia.getId(), inicio, fin, dias, vencimientoAnterior, membresia.getFechaVencimiento()));

        return toDto(congelamiento, membresia, hoy);
    }

    /**
     * Termina un congelamiento antes de tiempo. Los días que no se usaron se descuentan del vencimiento;
     * si aún no había empezado (o empezó hoy) se anula por completo.
     */
    @Transactional(rollbackFor = Exception.class)
    public CongelamientoResponseDto finalizar(Integer idCongelamiento, User usuarioActual) {
        CongelamientoMembresia congelamiento = congelamientoRepository.findByIdConMembresia(idCongelamiento)
                .orElseThrow(() -> new ResourceNotFoundException("Congelamiento no encontrado con ID: " + idCongelamiento));
        LocalDate hoy = LocalDate.now();

        if (congelamiento.isAnulado() || congelamiento.getFechaFin().isBefore(hoy)) {
            throw new RuntimeException("Este congelamiento ya terminó");
        }

        int diasNoUsados;
        if (!congelamiento.getFechaInicio().isBefore(hoy)) {
            diasNoUsados = congelamiento.getDias();
            congelamiento.setAnulado(true);
        } else {
            int diasUsados = (int) ChronoUnit.DAYS.between(congelamiento.getFechaInicio(), hoy);
            diasNoUsados = congelamiento.getDias() - diasUsados;
            congelamiento.setDias(diasUsados);
            congelamiento.setFechaFin(hoy.minusDays(1));
        }
        congelamientoRepository.save(congelamiento);

        Membresia membresia = congelamiento.getMembresia();
        LocalDate vencimientoAnterior = membresia.getFechaVencimiento();
        membresia.setFechaVencimiento(vencimientoAnterior.minusDays(diasNoUsados));
        if (membresia.getEstadoMembresia() != null && membresia.getEstadoMembresia().getId() == ESTADO_CONGELADA) {
            membresia.setEstadoMembresia(estado(ESTADO_ACTIVA, "ACTIVA"));
        }
        membresiaRepository.save(membresia);

        auditoriaService.registrar(usuarioActual, "congelamiento_membresia", "UPDATE", congelamiento.getId(),
                String.format("Congelamiento finalizado anticipadamente (membresía ID %d). Días devueltos: %d. Vencimiento %s -> %s",
                        membresia.getId(), diasNoUsados, vencimientoAnterior, membresia.getFechaVencimiento()));

        return toDto(congelamiento, membresia, hoy);
    }

    /** Si el socio tiene un congelamiento en curso, no puede registrar su ingreso. */
    public void validarSinCongelamientoActivo(Integer socioId, LocalDate fecha) {
        congelamientoRepository.findActivosBySocio(socioId, fecha).stream().findFirst().ifPresent(c -> {
            throw new RuntimeException(String.format(
                    "La membresía del socio está congelada del %s al %s. El ingreso se habilita al terminar el congelamiento o al descongelarla.",
                    c.getFechaInicio().format(FORMATO_FECHA), c.getFechaFin().format(FORMATO_FECHA)));
        });
    }

    /** Estado que debe tener una membresía pagada: CONGELADA si tiene un congelamiento en curso. */
    public boolean estaCongeladaEn(Integer idMembresia, LocalDate fecha) {
        return congelamientoRepository.existeActivoEn(idMembresia, fecha);
    }

    /** Ajusta los estados según la fecha: inicia los congelamientos programados y reactiva los terminados. */
    @Transactional(rollbackFor = Exception.class)
    public int sincronizarEstados(LocalDate hoy) {
        int cambios = 0;
        EstadoMembresia congelada = null;
        for (CongelamientoMembresia c : congelamientoRepository.findActivosEn(hoy)) {
            Membresia m = c.getMembresia();
            if (m.getEstadoMembresia() != null && m.getEstadoMembresia().getId() == ESTADO_ACTIVA) {
                if (congelada == null) congelada = estado(ESTADO_CONGELADA, "CONGELADA");
                m.setEstadoMembresia(congelada);
                membresiaRepository.save(m);
                cambios++;
            }
        }
        EstadoMembresia activa = null;
        for (Membresia m : membresiaRepository.findByEstadoMembresia_Id(ESTADO_CONGELADA)) {
            if (!congelamientoRepository.existeActivoEn(m.getId(), hoy)) {
                if (activa == null) activa = estado(ESTADO_ACTIVA, "ACTIVA");
                m.setEstadoMembresia(activa);
                membresiaRepository.save(m);
                cambios++;
            }
        }
        if (cambios > 0) {
            log.info("[CONGELAMIENTO] {} membresía(s) cambiaron de estado el {}", cambios, hoy);
        }
        return cambios;
    }

    private void validarMembresiaCongelable(Membresia membresia, LocalDate hoy) {
        Integer idEstado = membresia.getEstadoMembresia() != null ? membresia.getEstadoMembresia().getId() : null;
        if (idEstado != null && idEstado == ESTADO_CONGELADA) {
            throw new RuntimeException("La membresía ya se encuentra congelada");
        }
        if (idEstado == null || idEstado != ESTADO_ACTIVA) {
            throw new RuntimeException("Solo se pueden congelar membresías activas");
        }
        if (membresia.getFechaVencimiento() == null || membresia.getFechaVencimiento().isBefore(hoy)) {
            throw new RuntimeException("La membresía ya venció; renuévela antes de congelarla");
        }
    }

    private EstadoMembresia estado(int id, String nombre) {
        return estadoMembresiaRepository.findById(id)
                .orElseGet(() -> estadoMembresiaRepository.findByNombreIgnoreCase(nombre)
                        .orElseThrow(() -> new ResourceNotFoundException("Estado '" + nombre + "' no encontrado en el catálogo")));
    }

    private String nombreDe(User usuario) {
        if (usuario == null) return null;
        String nombre = ((usuario.getNombres() != null ? usuario.getNombres() : "") + " "
                + (usuario.getApellidos() != null ? usuario.getApellidos() : "")).trim();
        return nombre.isEmpty() ? usuario.getCorreo() : nombre;
    }

    private CongelamientoResponseDto toDto(CongelamientoMembresia c, Membresia membresia, LocalDate hoy) {
        return CongelamientoResponseDto.builder()
                .id(c.getId())
                .idMembresia(membresia != null ? membresia.getId() : null)
                .nombrePlan(membresia != null && membresia.getPlan() != null ? membresia.getPlan().getNombre() : null)
                .fechaInicio(c.getFechaInicio())
                .fechaFin(c.getFechaFin())
                .dias(c.getDias())
                .motivo(c.getMotivo())
                .estado(c.estadoEn(hoy))
                .fechaVencimientoMembresia(membresia != null ? membresia.getFechaVencimiento() : null)
                .fechaRegistro(c.getFechaRegistro())
                .registradoPor(c.getRegistradoPor())
                .build();
    }
}
