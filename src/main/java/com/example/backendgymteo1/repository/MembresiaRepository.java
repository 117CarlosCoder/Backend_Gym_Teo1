package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Integer> {
}
