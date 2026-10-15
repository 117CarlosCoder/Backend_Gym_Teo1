package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    @Query("SELECT p FROM Promocion p WHERE p.activo = true AND p.fechaInicio <= :fecha AND p.fechaFin >= :fecha")
    List<Promocion> findVigentes(@Param("fecha") LocalDate fecha);
}
