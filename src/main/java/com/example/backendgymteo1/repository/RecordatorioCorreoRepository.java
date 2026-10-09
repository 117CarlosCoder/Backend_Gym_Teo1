package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.RecordatorioCorreo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordatorioCorreoRepository extends JpaRepository<RecordatorioCorreo, Integer> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT r.id FROM RecordatorioCorreo r " +
            "WHERE r.intentos < r.maxIntentos " +
            "  AND r.proximaEjecucion <= :ahora " +
            "  AND (" +
            "    r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.PENDIENTE " +
            "    OR r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.FALLIDO " +
            "    OR (r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.ENVIANDO AND r.lockedAt < :staleThreshold)" +
            "  ) " +
            "ORDER BY r.proximaEjecucion ASC")
    List<Integer> findCandidateIdsParaEnvio(
            @Param("ahora") LocalDateTime ahora,
            @Param("staleThreshold") LocalDateTime staleThreshold,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE RecordatorioCorreo r " +
            "SET r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.ENVIANDO, " +
            "    r.lockedAt = :ahora, " +
            "    r.ultimoIntento = :ahora, " +
            "    r.intentos = r.intentos + 1, " +
            "    r.claimToken = :claimToken " +
            "WHERE r.id = :id " +
            "  AND r.intentos < r.maxIntentos " +
            "  AND r.proximaEjecucion <= :ahora " +
            "  AND (" +
            "     r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.PENDIENTE " +
            "     OR r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.FALLIDO " +
            "     OR (r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.ENVIANDO AND r.lockedAt < :staleThreshold)" +
            "  )")
    int claimRecordatorio(
            @Param("id") Integer id,
            @Param("ahora") LocalDateTime ahora,
            @Param("staleThreshold") LocalDateTime staleThreshold,
            @Param("claimToken") String claimToken
    );

    @Modifying
    @Query("UPDATE RecordatorioCorreo r " +
            "SET r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.ENVIADO, " +
            "    r.fechaEnvio = :ahora, " +
            "    r.proveedorMensajeId = :proveedorMensajeId, " +
            "    r.lockedAt = NULL, " +
            "    r.claimToken = NULL, " +
            "    r.errorMensaje = NULL " +
            "WHERE r.id = :id AND r.claimToken = :claimToken")
    int marcarExitoConFence(
            @Param("id") Integer id,
            @Param("claimToken") String claimToken,
            @Param("proveedorMensajeId") String proveedorMensajeId,
            @Param("ahora") LocalDateTime ahora
    );

    @Modifying
    @Query("UPDATE RecordatorioCorreo r " +
            "SET r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.FALLIDO, " +
            "    r.proximaEjecucion = :proximaEjecucion, " +
            "    r.errorMensaje = :errorMensaje, " +
            "    r.lockedAt = NULL, " +
            "    r.claimToken = NULL " +
            "WHERE r.id = :id AND r.claimToken = :claimToken")
    int marcarFalloConFence(
            @Param("id") Integer id,
            @Param("claimToken") String claimToken,
            @Param("proximaEjecucion") LocalDateTime proximaEjecucion,
            @Param("errorMensaje") String errorMensaje
    );

    @Modifying
    @Query("UPDATE RecordatorioCorreo r " +
            "SET r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.FALLIDO, " +
            "    r.intentos = r.maxIntentos, " +
            "    r.errorMensaje = :errorMensaje, " +
            "    r.lockedAt = NULL, " +
            "    r.claimToken = NULL " +
            "WHERE r.id = :id AND r.claimToken = :claimToken")
    int marcarFalloDefinitivoConFence(
            @Param("id") Integer id,
            @Param("claimToken") String claimToken,
            @Param("errorMensaje") String errorMensaje
    );

    @Modifying
    @Query("UPDATE RecordatorioCorreo r " +
            "SET r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.FALLIDO, " +
            "    r.errorMensaje = 'Reclamo final abandonado o expirado sin confirmación de envío', " +
            "    r.lockedAt = NULL, " +
            "    r.claimToken = NULL " +
            "WHERE r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.ENVIANDO " +
            "  AND r.lockedAt < :staleThreshold " +
            "  AND r.intentos >= r.maxIntentos")
    int terminarReclamosAgotadosStale(@Param("staleThreshold") LocalDateTime staleThreshold);

    @Modifying
    @Query("UPDATE RecordatorioCorreo r " +
            "SET r.estado = com.example.backendgymteo1.entity.EstadoRecordatorioCorreo.FALLIDO, " +
            "    r.intentos = r.maxIntentos, " +
            "    r.errorMensaje = :errorMensaje, " +
            "    r.lockedAt = NULL, " +
            "    r.claimToken = NULL " +
            "WHERE r.id = :id AND r.claimToken = :claimToken")
    int marcarObsoletoConFence(
            @Param("id") Integer id,
            @Param("claimToken") String claimToken,
            @Param("errorMensaje") String errorMensaje
    );

    @Query("SELECT r FROM RecordatorioCorreo r " +
            "JOIN FETCH r.notificacion n " +
            "JOIN FETCH r.membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "WHERE r.id = :id")
    Optional<RecordatorioCorreo> findByIdWithDetails(@Param("id") Integer id);
}
