package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Bitacora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraRepository extends JpaRepository<Bitacora, Integer> {
    List<Bitacora> findByNombreTablaAndIdRegistroOrderByFechaHoraDesc(String nombreTabla, Integer idRegistro);
    Page<Bitacora> findByUsuarioIdOrderByFechaHoraDesc(Integer usuarioId, Pageable pageable);
}
