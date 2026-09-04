package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {
    Optional<Sucursal> findByNombreIgnoreCase(String nombre);
    List<Sucursal> findByActivoTrue();
}
