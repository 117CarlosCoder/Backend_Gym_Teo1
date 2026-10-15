package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Asistencia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    @Query("SELECT a FROM Asistencia a LEFT JOIN FETCH a.recepcionista r LEFT JOIN FETCH r.usuario LEFT JOIN FETCH a.usuarioRegistro ur LEFT JOIN FETCH ur.rol LEFT JOIN FETCH a.sucursal WHERE a.socio.id = :socioId ORDER BY a.fecha DESC, a.horaEntrada DESC LIMIT 1")
    Optional<Asistencia> findUltimaAsistenciaBySocioId(@Param("socioId") Integer socioId);

    @Query("SELECT a FROM Asistencia a LEFT JOIN FETCH a.recepcionista r LEFT JOIN FETCH r.usuario LEFT JOIN FETCH a.usuarioRegistro ur LEFT JOIN FETCH ur.rol LEFT JOIN FETCH a.sucursal WHERE a.socio.id = :socioId ORDER BY a.fecha DESC, a.horaEntrada DESC")
    List<Asistencia> findBySocioIdOrderByFechaDescHoraEntradaDesc(@Param("socioId") Integer socioId);

    @Query("SELECT a.fecha, COUNT(a) FROM Asistencia a LEFT JOIN a.sucursal s " +
            "WHERE a.fecha BETWEEN :desde AND :hasta AND (:idSucursal IS NULL OR s.id = :idSucursal) " +
            "GROUP BY a.fecha ORDER BY a.fecha")
    List<Object[]> contarPorDia(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta,
                                @Param("idSucursal") Integer idSucursal);

    @Query("SELECT s.id, s.nombre, COUNT(a) FROM Asistencia a LEFT JOIN a.sucursal s " +
            "WHERE a.fecha BETWEEN :desde AND :hasta AND (:idSucursal IS NULL OR s.id = :idSucursal) " +
            "GROUP BY s.id, s.nombre ORDER BY COUNT(a) DESC")
    List<Object[]> contarPorSucursal(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta,
                                     @Param("idSucursal") Integer idSucursal);

    @Query("SELECT COUNT(DISTINCT a.socio.id) FROM Asistencia a LEFT JOIN a.sucursal s " +
            "WHERE a.fecha BETWEEN :desde AND :hasta AND (:idSucursal IS NULL OR s.id = :idSucursal)")
    long contarSociosUnicos(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta,
                            @Param("idSucursal") Integer idSucursal);

    long countByFecha(LocalDate fecha);

    long countByFechaBetween(LocalDate desde, LocalDate hasta);

    @Query("SELECT HOUR(a.horaEntrada), COUNT(a) FROM Asistencia a " +
            "WHERE a.fecha BETWEEN :desde AND :hasta " +
            "GROUP BY HOUR(a.horaEntrada) " +
            "ORDER BY HOUR(a.horaEntrada) ASC")
    List<Object[]> findHorasPicoBetween(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("SELECT suc.id, suc.nombre, COUNT(a) FROM Asistencia a JOIN a.sucursal suc " +
            "WHERE a.fecha BETWEEN :desde AND :hasta " +
            "GROUP BY suc.id, suc.nombre " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> findAsistenciasPorSucursalBetween(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("SELECT s.id, u.nombres, u.apellidos, COUNT(a) FROM Asistencia a JOIN a.socio s JOIN s.usuario u " +
            "WHERE a.fecha BETWEEN :desde AND :hasta " +
            "GROUP BY s.id, u.nombres, u.apellidos " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> findTopSociosAsistenciasBetween(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, Pageable pageable);

    @Query("SELECT a.fecha, COUNT(a) FROM Asistencia a " +
            "WHERE a.fecha BETWEEN :desde AND :hasta " +
            "GROUP BY a.fecha " +
            "ORDER BY a.fecha ASC")
    List<Object[]> findAsistenciasDiariasBetween(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    long countBySucursalIdAndFecha(Integer sucursalId, LocalDate fecha);

    long countBySucursalIdAndFechaBetween(Integer sucursalId, LocalDate desde, LocalDate hasta);

    @Query("SELECT COUNT(a) FROM Asistencia a WHERE (a.recepcionista.id = :usuarioId OR a.usuarioRegistro.id = :usuarioId) AND a.fecha = :fecha")
    long countByUsuarioRegistroOrRecepcionistaAndFecha(@Param("usuarioId") Integer usuarioId, @Param("fecha") LocalDate fecha);

    @Query("SELECT HOUR(a.horaEntrada), COUNT(a) FROM Asistencia a " +
            "WHERE a.sucursal.id = :sucursalId AND a.fecha = :fecha " +
            "GROUP BY HOUR(a.horaEntrada) " +
            "ORDER BY HOUR(a.horaEntrada) ASC")
    List<Object[]> findHorasPicoBySucursalAndFecha(@Param("sucursalId") Integer sucursalId, @Param("fecha") LocalDate fecha);

    @Query("SELECT a FROM Asistencia a JOIN FETCH a.socio s JOIN FETCH s.usuario u JOIN FETCH a.sucursal " +
            "WHERE a.sucursal.id = :sucursalId AND a.fecha = :fecha " +
            "ORDER BY a.horaEntrada DESC")
    List<Asistencia> findUltimasAsistenciasBySucursalAndFecha(@Param("sucursalId") Integer sucursalId, @Param("fecha") LocalDate fecha, Pageable pageable);

    long countBySocioId(Integer socioId);

    long countBySocioIdAndFechaBetween(Integer socioId, LocalDate desde, LocalDate hasta);

    @Query("SELECT YEAR(a.fecha), MONTH(a.fecha), COUNT(a) FROM Asistencia a " +
            "WHERE a.socio.id = :socioId AND a.fecha >= :desde " +
            "GROUP BY YEAR(a.fecha), MONTH(a.fecha) " +
            "ORDER BY YEAR(a.fecha) DESC, MONTH(a.fecha) DESC")
    List<Object[]> findVisitasMesBySocioId(@Param("socioId") Integer socioId, @Param("desde") LocalDate desde);

    @Query("SELECT HOUR(a.horaEntrada), COUNT(a) FROM Asistencia a " +
            "WHERE a.socio.id = :socioId " +
            "GROUP BY HOUR(a.horaEntrada) " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> findHorasAsistenciaBySocioId(@Param("socioId") Integer socioId);

    @Query("SELECT DISTINCT a.fecha FROM Asistencia a WHERE a.socio.id = :socioId ORDER BY a.fecha DESC")
    List<LocalDate> findFechasAsistenciaBySocioId(@Param("socioId") Integer socioId);

    @Query("SELECT a.socio.id, MAX(a.fecha) FROM Asistencia a GROUP BY a.socio.id")
    List<Object[]> findUltimaFechaAsistenciaPorSocio();

    @Query("SELECT a FROM Asistencia a " +
            "JOIN FETCH a.socio s " +
            "JOIN FETCH s.usuario u " +
            "LEFT JOIN FETCH a.sucursal suc " +
            "WHERE (:sucursalId IS NULL OR suc.id = :sucursalId) " +
            "AND a.fecha = :fecha " +
            "ORDER BY a.horaEntrada DESC")
    List<Asistencia> findUltimasAsistenciasConDetalles(@Param("sucursalId") Integer sucursalId, @Param("fecha") LocalDate fecha, Pageable pageable);
}
