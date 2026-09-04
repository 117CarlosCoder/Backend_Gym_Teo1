package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Integer> {

    @Query("SELECT m FROM Membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH m.estadoMembresia e " +
            "WHERE m.id = :id")
    Optional<Membresia> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT m FROM Membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH m.estadoMembresia e " +
            "WHERE (:socioId IS NULL OR s.id = :socioId) " +
            "AND (:planId IS NULL OR p.id = :planId) " +
            "AND (:estadoId IS NULL OR e.id = :estadoId) " +
            "AND (:fechaDesde IS NULL OR m.fechaInicio >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR m.fechaVencimiento <= :fechaHasta) " +
            "ORDER BY m.fechaInicio DESC")
    List<Membresia> findWithFilters(
            @Param("socioId") Integer socioId,
            @Param("planId") Integer planId,
            @Param("estadoId") Integer estadoId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("SELECT m FROM Membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH m.estadoMembresia e " +
            "WHERE s.id = :socioId " +
            "ORDER BY m.fechaInicio DESC")
    List<Membresia> findBySocioIdWithDetails(@Param("socioId") Integer socioId);

    @Query("SELECT COUNT(m) > 0 FROM Membresia m " +
            "WHERE m.socio.id = :socioId " +
            "AND m.estadoMembresia.id = :estadoId " +
            "AND m.fechaVencimiento >= :fechaActual")
    boolean existsActiveBySocio(
            @Param("socioId") Integer socioId,
            @Param("estadoId") Integer estadoId,
            @Param("fechaActual") LocalDate fechaActual
    );

    @Query("SELECT m FROM Membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH m.estadoMembresia e " +
            "WHERE s.id = :socioId " +
            "AND u.estado = true " +
            "AND u.eliminadoEn IS NULL " +
            "AND e.id = 1 " +
            "AND m.fechaVencimiento >= :fechaActual " +
            "ORDER BY m.fechaVencimiento DESC")
    Optional<Membresia> findActiveBySocioId(
            @Param("socioId") Integer socioId,
            @Param("fechaActual") LocalDate fechaActual
    );
}
