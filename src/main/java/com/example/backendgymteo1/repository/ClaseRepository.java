package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Integer> {
}
