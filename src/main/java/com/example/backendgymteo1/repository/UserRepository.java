package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByCorreo(String correo);
    List<User> findByEstadoTrue();
    Optional<User> findByIdAndEstadoTrue(Integer id);
    boolean existsByCorreo(String correo);
    boolean existsByDpi(String dpi);
}
