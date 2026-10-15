package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistema, Integer> {

    Optional<ConfiguracionSistema> findByClave(String clave);
}
