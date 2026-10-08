package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.EstadoFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoFacturaRepository extends JpaRepository<EstadoFactura, Integer> {

    Optional<EstadoFactura> findByNombreIgnoreCase(String nombre);
}
