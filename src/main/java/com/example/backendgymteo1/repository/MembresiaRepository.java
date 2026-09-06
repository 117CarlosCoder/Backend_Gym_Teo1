package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Membresia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Integer> {

    @Query("SELECT DISTINCT m FROM Membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH m.estadoMembresia e " +
            "LEFT JOIN FETCH m.sucursales " +
            "WHERE m.id = :id")
    Optional<Membresia> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT DISTINCT m FROM Membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH m.estadoMembresia e " +
            "LEFT JOIN FETCH m.sucursales suc " +
            "WHERE (:socioId IS NULL OR s.id = :socioId) " +
            "AND (:planId IS NULL OR p.id = :planId) " +
            "AND (:sucursalId IS NULL OR suc.id = :sucursalId) " +
            "AND (:estadoId IS NULL OR e.id = :estadoId) " +
            "AND (:estadoNombre IS NULL OR LOWER(e.nombre) = LOWER(:estadoNombre)) " +
            "AND (:fechaDesde IS NULL OR m.fechaInicio >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR m.fechaVencimiento <= :fechaHasta) " +
            "ORDER BY m.fechaInicio DESC")
    List<Membresia> findWithFilters(
            @Param("socioId") Integer socioId,
            @Param("planId") Integer planId,
            @Param("sucursalId") Integer sucursalId,
            @Param("estadoId") Integer estadoId,
            @Param("estadoNombre") String estadoNombre,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta
    );

    @EntityGraph(attributePaths = {"socio", "socio.usuario", "plan", "estadoMembresia", "sucursales"})
    @Query(value = "SELECT DISTINCT m FROM Membresia m " +
            "LEFT JOIN m.sucursales suc " +
            "WHERE (:socioId IS NULL OR m.socio.id = :socioId) " +
            "AND (:planId IS NULL OR m.plan.id = :planId) " +
            "AND (:sucursalId IS NULL OR suc.id = :sucursalId) " +
            "AND (:estadoId IS NULL OR m.estadoMembresia.id = :estadoId) " +
            "AND (:estadoNombre IS NULL OR LOWER(m.estadoMembresia.nombre) = LOWER(:estadoNombre)) " +
            "AND (:fechaDesde IS NULL OR m.fechaInicio >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR m.fechaVencimiento <= :fechaHasta)",
            countQuery = "SELECT COUNT(DISTINCT m) FROM Membresia m " +
                    "LEFT JOIN m.sucursales suc " +
                    "WHERE (:socioId IS NULL OR m.socio.id = :socioId) " +
                    "AND (:planId IS NULL OR m.plan.id = :planId) " +
                    "AND (:sucursalId IS NULL OR suc.id = :sucursalId) " +
                    "AND (:estadoId IS NULL OR m.estadoMembresia.id = :estadoId) " +
                    "AND (:estadoNombre IS NULL OR LOWER(m.estadoMembresia.nombre) = LOWER(:estadoNombre)) " +
                    "AND (:fechaDesde IS NULL OR m.fechaInicio >= :fechaDesde) " +
                    "AND (:fechaHasta IS NULL OR m.fechaVencimiento <= :fechaHasta)")
    Page<Membresia> findWithFiltersPaged(
            @Param("socioId") Integer socioId,
            @Param("planId") Integer planId,
            @Param("sucursalId") Integer sucursalId,
            @Param("estadoId") Integer estadoId,
            @Param("estadoNombre") String estadoNombre,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable
    );

    @Query("SELECT DISTINCT m FROM Membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH m.estadoMembresia e " +
            "LEFT JOIN FETCH m.sucursales " +
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

    @Query("SELECT DISTINCT m FROM Membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH m.estadoMembresia e " +
            "LEFT JOIN FETCH m.sucursales " +
            "WHERE s.id = :socioId " +
            "AND u.estado = true " +
            "AND u.eliminadoEn IS NULL " +
            "AND e.id = 1 " +
            "AND m.fechaVencimiento >= :fechaActual " +
            "ORDER BY m.fechaVencimiento DESC")
    Optional<Membresia> findActiveBySocioId(
            @Param("socioId") Integer socioId,
            @Param("fechaActual") LocalDate fechaActualmembresia_sucursal
    );
}
