package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    @Query("SELECT a FROM Asistencia a LEFT JOIN FETCH a.recepcionista r LEFT JOIN FETCH r.usuario WHERE a.socio.id = :socioId ORDER BY a.fecha DESC, a.horaEntrada DESC LIMIT 1")
    Optional<Asistencia> findUltimaAsistenciaBySocioId(@Param("socioId") Integer socioId);
}
