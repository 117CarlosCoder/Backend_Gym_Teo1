package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.CongelamientoMembresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CongelamientoMembresiaRepository extends JpaRepository<CongelamientoMembresia, Integer> {

    @Query("SELECT c FROM CongelamientoMembresia c " +
            "WHERE c.membresia.id = :idMembresia " +
            "ORDER BY c.fechaInicio DESC")
    List<CongelamientoMembresia> findByMembresiaId(@Param("idMembresia") Integer idMembresia);

    @Query("SELECT c FROM CongelamientoMembresia c " +
            "JOIN FETCH c.membresia m " +
            "JOIN FETCH m.plan p " +
            "WHERE m.socio.id = :socioId " +
            "ORDER BY c.fechaInicio DESC")
    List<CongelamientoMembresia> findBySocioId(@Param("socioId") Integer socioId);

    @Query("SELECT c FROM CongelamientoMembresia c " +
            "JOIN FETCH c.membresia m " +
            "JOIN FETCH m.estadoMembresia e " +
            "WHERE c.id = :id")
    Optional<CongelamientoMembresia> findByIdConMembresia(@Param("id") Integer id);

    /** Congelamientos vigentes (no anulados) que se cruzan con el rango indicado. */
    @Query("SELECT COUNT(c) > 0 FROM CongelamientoMembresia c " +
            "WHERE c.membresia.id = :idMembresia AND c.anulado = false " +
            "AND c.fechaInicio <= :fin AND c.fechaFin >= :inicio")
    boolean existeTraslape(@Param("idMembresia") Integer idMembresia,
                           @Param("inicio") LocalDate inicio,
                           @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(c) FROM CongelamientoMembresia c " +
            "WHERE c.membresia.id = :idMembresia AND c.anulado = false " +
            "AND YEAR(c.fechaInicio) = :anio")
    long contarEnAnio(@Param("idMembresia") Integer idMembresia, @Param("anio") int anio);

    /** Congelamiento en curso en la fecha indicada para alguna membresía del socio. */
    @Query("SELECT c FROM CongelamientoMembresia c " +
            "WHERE c.membresia.socio.id = :socioId AND c.anulado = false " +
            "AND c.fechaInicio <= :fecha AND c.fechaFin >= :fecha")
    List<CongelamientoMembresia> findActivosBySocio(@Param("socioId") Integer socioId, @Param("fecha") LocalDate fecha);

    @Query("SELECT c FROM CongelamientoMembresia c " +
            "JOIN FETCH c.membresia m " +
            "JOIN FETCH m.estadoMembresia e " +
            "WHERE c.anulado = false AND c.fechaInicio <= :fecha AND c.fechaFin >= :fecha")
    List<CongelamientoMembresia> findActivosEn(@Param("fecha") LocalDate fecha);

    @Query("SELECT COUNT(c) > 0 FROM CongelamientoMembresia c " +
            "WHERE c.membresia.id = :idMembresia AND c.anulado = false " +
            "AND c.fechaInicio <= :fecha AND c.fechaFin >= :fecha")
    boolean existeActivoEn(@Param("idMembresia") Integer idMembresia, @Param("fecha") LocalDate fecha);
}
