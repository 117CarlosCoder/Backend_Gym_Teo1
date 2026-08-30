package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.AsistenciaClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsistenciaClaseRepository extends JpaRepository<AsistenciaClase, Integer> {
}
