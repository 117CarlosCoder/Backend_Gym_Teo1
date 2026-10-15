package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Asistencia;
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
}
