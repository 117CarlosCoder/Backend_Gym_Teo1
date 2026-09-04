package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Socio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Integer> {

    List<Socio> findByUsuarioEstadoTrue();

    Optional<Socio> findByIdAndUsuarioEstadoTrue(Integer id);

    @Query("SELECT s FROM Socio s JOIN FETCH s.usuario u LEFT JOIN FETCH s.sucursal WHERE u.estado = true")
    List<Socio> findAllActiveWithUser();

    @Query(value = "SELECT s FROM Socio s JOIN FETCH s.usuario u LEFT JOIN FETCH s.sucursal WHERE u.estado = true",
           countQuery = "SELECT COUNT(s) FROM Socio s JOIN s.usuario u WHERE u.estado = true")
    Page<Socio> findAllActiveWithUser(Pageable pageable);

    @Query("SELECT s FROM Socio s JOIN FETCH s.usuario u LEFT JOIN FETCH s.sucursal WHERE s.id = :id AND u.estado = true")
    Optional<Socio> findByIdAndActiveWithUser(@Param("id") Integer id);

    @Query("SELECT s FROM Socio s JOIN FETCH s.usuario u LEFT JOIN FETCH s.sucursal WHERE u.correo = :correo AND u.estado = true")
    Optional<Socio> findByCorreoAndActiveWithUser(@Param("correo") String correo);

    @Query("SELECT s FROM Socio s JOIN FETCH s.usuario u LEFT JOIN FETCH s.sucursal WHERE u.dpi = :dpi AND u.estado = true")
    Optional<Socio> findByDpiAndActiveWithUser(@Param("dpi") String dpi);
}
