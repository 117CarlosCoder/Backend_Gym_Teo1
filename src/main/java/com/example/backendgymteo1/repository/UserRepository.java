package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByCorreo(String correo);
    List<User> findByEstadoTrue();
    Optional<User> findByIdAndEstadoTrue(Integer id);
    List<User> findByEliminadoEnIsNull();
    Optional<User> findByIdAndEliminadoEnIsNull(Integer id);
    boolean existsByCorreo(String correo);
    boolean existsByDpi(String dpi);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.estado = false, u.eliminadoEn = :now, u.correo = null, u.dpi = null, u.username = null WHERE u.id = :id AND u.eliminadoEn IS NULL")
    int softDeleteUser(@Param("id") Integer id, @Param("now") LocalDateTime now);
}
