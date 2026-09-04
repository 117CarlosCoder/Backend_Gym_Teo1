package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.PlanMembresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanMembresiaRepository extends JpaRepository<PlanMembresia, Integer> {

    Optional<PlanMembresia> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}
