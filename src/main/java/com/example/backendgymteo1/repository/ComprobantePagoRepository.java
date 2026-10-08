package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.ComprobantePago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobantePagoRepository extends JpaRepository<ComprobantePago, Integer> {

    @Query("SELECT c FROM ComprobantePago c " +
            "JOIN FETCH c.factura f " +
            "JOIN FETCH f.membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH c.metodoPago mp " +
            "WHERE c.id = :id")
    Optional<ComprobantePago> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT c FROM ComprobantePago c " +
            "JOIN FETCH c.factura f " +
            "JOIN FETCH f.membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH c.metodoPago mp " +
            "WHERE s.id = :socioId " +
            "ORDER BY c.fechaPago DESC")
    List<ComprobantePago> findBySocioIdWithDetails(@Param("socioId") Integer socioId);

    @EntityGraph(attributePaths = {"factura", "factura.membresia", "factura.membresia.socio", "factura.membresia.socio.usuario", "factura.membresia.plan", "metodoPago"})
    @Query(value = "SELECT c FROM ComprobantePago c " +
            "WHERE c.factura.membresia.socio.id = :socioId " +
            "ORDER BY c.fechaPago DESC",
            countQuery = "SELECT COUNT(c) FROM ComprobantePago c WHERE c.factura.membresia.socio.id = :socioId")
    Page<ComprobantePago> findBySocioIdPaged(@Param("socioId") Integer socioId, Pageable pageable);
}
