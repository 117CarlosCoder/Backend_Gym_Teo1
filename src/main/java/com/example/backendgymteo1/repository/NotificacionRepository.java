package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.Notificacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    @EntityGraph(attributePaths = {"membresia"})
    @Query("SELECT n FROM Notificacion n " +
            "WHERE n.usuario.id = :usuarioId " +
            "AND (:leida IS NULL OR n.leida = :leida) " +
            "ORDER BY n.fechaCreacion DESC, n.id DESC")
    List<Notificacion> findByUsuarioIdAndLeidaOptional(
            @Param("usuarioId") Integer usuarioId,
            @Param("leida") Boolean leida
    );

    long countByUsuarioIdAndLeidaFalse(Integer usuarioId);

    @EntityGraph(attributePaths = {"membresia"})
    Optional<Notificacion> findByIdAndUsuarioId(Integer id, Integer usuarioId);

    boolean existsByMembresiaIdAndFechaVencimientoAndUmbralDias(
            Integer membresiaId,
            LocalDate fechaVencimiento,
            Integer umbralDias
    );

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("UPDATE Notificacion n SET n.leida = true, n.fechaLectura = :fechaLectura " +
            "WHERE n.id = :id AND n.usuario.id = :usuarioId AND n.leida = false")
    int marcarComoLeidaSiNoLeida(
            @Param("id") Integer id,
            @Param("usuarioId") Integer usuarioId,
            @Param("fechaLectura") java.time.LocalDateTime fechaLectura
    );
}
