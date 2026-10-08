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
}
