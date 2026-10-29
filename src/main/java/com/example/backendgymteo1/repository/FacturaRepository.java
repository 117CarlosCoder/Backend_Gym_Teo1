package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    boolean existsByIdPromocion(Integer idPromocion);

    @Query("SELECT f FROM Factura f " +
            "JOIN FETCH f.membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH f.estadoFactura ef " +
            "WHERE f.id = :id")
    Optional<Factura> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT f FROM Factura f " +
            "JOIN FETCH f.membresia m " +
            "JOIN FETCH m.socio s " +
            "WHERE s.id = :socioId " +
            "ORDER BY f.fechaEmision DESC")
    List<Factura> findBySocioId(@Param("socioId") Integer socioId);

    // --- Pagos en abonos y estado de cuenta ---

    @Query("SELECT f FROM Factura f " +
            "JOIN FETCH f.estadoFactura ef " +
            "WHERE f.membresia.id = :idMembresia AND (ef.id = 1 OR UPPER(ef.nombre) = 'PENDIENTE') " +
            "ORDER BY f.fechaEmision ASC, f.id ASC")
    List<Factura> findPendientesByMembresiaId(@Param("idMembresia") Integer idMembresia);

    @Query("SELECT f FROM Factura f " +
            "JOIN FETCH f.estadoFactura ef " +
            "WHERE f.membresia.socio.id = :socioId AND (ef.id = 1 OR UPPER(ef.nombre) = 'PENDIENTE') " +
            "ORDER BY f.fechaVencimiento ASC, f.id ASC")
    List<Factura> findPendientesBySocioId(@Param("socioId") Integer socioId);

    @Query("SELECT f FROM Factura f " +
            "JOIN FETCH f.membresia m " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH f.estadoFactura ef " +
            "WHERE m.socio.id = :socioId " +
            "ORDER BY f.fechaEmision DESC, f.id DESC")
    List<Factura> findEstadoCuentaBySocioId(@Param("socioId") Integer socioId);
}
