package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.EstadoMembresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoMembresiaRepository extends JpaRepository<EstadoMembresia, Integer> {

    Optional<EstadoMembresia> findByNombreIgnoreCase(String nombre);
}
