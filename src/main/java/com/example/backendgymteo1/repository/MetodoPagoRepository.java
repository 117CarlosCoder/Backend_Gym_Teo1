package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

    Optional<MetodoPago> findByNombreIgnoreCase(String nombre);
}
