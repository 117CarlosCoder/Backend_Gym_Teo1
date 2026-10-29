package com.example.backendgymteo1.repository;

import com.example.backendgymteo1.entity.ComprobantePago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobantePagoRepository extends JpaRepository<ComprobantePago, Integer> {

    @Query("SELECT c FROM ComprobantePago c " +
            "JOIN FETCH c.factura f " +
            "JOIN FETCH f.membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH c.metodoPago mp " +
            "LEFT JOIN FETCH f.recepcionista r " +
            "LEFT JOIN FETCH r.usuario ru " +
            "WHERE c.id = :id")
    Optional<ComprobantePago> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT c FROM ComprobantePago c " +
            "JOIN FETCH c.factura f " +
            "JOIN FETCH f.membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH c.metodoPago mp " +
            "LEFT JOIN FETCH f.recepcionista r " +
            "LEFT JOIN FETCH r.usuario ru " +
            "WHERE s.id = :socioId " +
            "ORDER BY c.fechaPago DESC")
    List<ComprobantePago> findBySocioIdWithDetails(@Param("socioId") Integer socioId);

    @EntityGraph(attributePaths = {"factura", "factura.membresia", "factura.membresia.socio", "factura.membresia.socio.usuario", "factura.membresia.plan", "factura.recepcionista", "factura.recepcionista.usuario", "metodoPago"})
    @Query(value = "SELECT c FROM ComprobantePago c " +
            "WHERE c.factura.membresia.socio.id = :socioId " +
            "ORDER BY c.fechaPago DESC",
            countQuery = "SELECT COUNT(c) FROM ComprobantePago c WHERE c.factura.membresia.socio.id = :socioId")
    Page<ComprobantePago> findBySocioIdPaged(@Param("socioId") Integer socioId, Pageable pageable);

    @EntityGraph(attributePaths = {"factura", "factura.membresia", "factura.membresia.socio", "factura.membresia.socio.usuario", "factura.membresia.plan", "factura.recepcionista", "factura.recepcionista.usuario", "metodoPago"})
    @Query(value = "SELECT c FROM ComprobantePago c " +
            "WHERE (:socioId IS NULL OR c.factura.membresia.socio.id = :socioId) " +
            "AND (:desde IS NULL OR c.fechaPago >= :desde) " +
            "AND (:hastaExclusivo IS NULL OR c.fechaPago < :hastaExclusivo) " +
            "AND (:idMetodoPago IS NULL OR c.metodoPago.id = :idMetodoPago)",
            countQuery = "SELECT COUNT(c) FROM ComprobantePago c " +
                    "WHERE (:socioId IS NULL OR c.factura.membresia.socio.id = :socioId) " +
                    "AND (:desde IS NULL OR c.fechaPago >= :desde) " +
                    "AND (:hastaExclusivo IS NULL OR c.fechaPago < :hastaExclusivo) " +
                    "AND (:idMetodoPago IS NULL OR c.metodoPago.id = :idMetodoPago)")
    Page<ComprobantePago> buscarHistorial(@Param("socioId") Integer socioId,
                                          @Param("desde") LocalDateTime desde,
                                          @Param("hastaExclusivo") LocalDateTime hastaExclusivo,
                                          @Param("idMetodoPago") Integer idMetodoPago,
                                          Pageable pageable);

    @Query("SELECT YEAR(c.fechaPago), MONTH(c.fechaPago), SUM(c.montoPagado), COUNT(c) " +
            "FROM ComprobantePago c " +
            "WHERE (:anio IS NULL OR YEAR(c.fechaPago) = :anio) " +
            "GROUP BY YEAR(c.fechaPago), MONTH(c.fechaPago) " +
            "ORDER BY YEAR(c.fechaPago) DESC, MONTH(c.fechaPago) DESC")
    List<Object[]> findIngresosMensuales(@Param("anio") Integer anio);

    @Query("SELECT mp.nombre, SUM(c.montoPagado), COUNT(c) " +
            "FROM ComprobantePago c JOIN c.metodoPago mp " +
            "WHERE (:anio IS NULL OR YEAR(c.fechaPago) = :anio) " +
            "GROUP BY mp.nombre " +
            "ORDER BY SUM(c.montoPagado) DESC")
    List<Object[]> findIngresosPorMetodoPago(@Param("anio") Integer anio);

    @Query("SELECT COALESCE(SUM(c.montoPagado), 0) FROM ComprobantePago c WHERE c.fechaPago BETWEEN :desde AND :hasta")
    BigDecimal sumMontoPagadoBetween(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(c) FROM ComprobantePago c WHERE c.fechaPago BETWEEN :desde AND :hasta")
    Long countPagosBetween(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(c.montoPagado), 0) FROM ComprobantePago c")
    BigDecimal sumTotalMonto();

    @Query("SELECT COUNT(c) FROM ComprobantePago c")
    Long countTotalPagos();

    @Query("SELECT c FROM ComprobantePago c " +
            "JOIN FETCH c.factura f " +
            "JOIN FETCH f.membresia m " +
            "JOIN FETCH m.socio s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH m.plan p " +
            "JOIN FETCH c.metodoPago mp " +
            "WHERE (:anio IS NULL OR YEAR(c.fechaPago) = :anio) " +
            "ORDER BY c.fechaPago DESC")
    List<ComprobantePago> findPagosDetallados(@Param("anio") Integer anio, Pageable pageable);

    // --- Pagos en abonos: saldo de cada factura ---

    @Query("SELECT COALESCE(SUM(c.montoPagado), 0) FROM ComprobantePago c WHERE c.factura.id = :idFactura")
    BigDecimal sumPagadoPorFactura(@Param("idFactura") Integer idFactura);

    /** Lo pagado en la factura hasta (e incluyendo) el comprobante indicado. */
    @Query("SELECT COALESCE(SUM(c.montoPagado), 0) FROM ComprobantePago c " +
            "WHERE c.factura.id = :idFactura AND c.id <= :idComprobante")
    BigDecimal sumPagadoHasta(@Param("idFactura") Integer idFactura, @Param("idComprobante") Integer idComprobante);

    @Query("SELECT COUNT(c) FROM ComprobantePago c WHERE c.factura.id = :idFactura AND c.id <= :idComprobante")
    long contarHasta(@Param("idFactura") Integer idFactura, @Param("idComprobante") Integer idComprobante);

    @Query("SELECT c FROM ComprobantePago c " +
            "JOIN FETCH c.metodoPago mp " +
            "WHERE c.factura.id IN :idsFactura " +
            "ORDER BY c.fechaPago ASC, c.id ASC")
    List<ComprobantePago> findByFacturaIds(@Param("idsFactura") List<Integer> idsFactura);
}
