package com.spring.boot.carro.circuito_manejo.persistence.repository;

import com.spring.boot.carro.circuito_manejo.persistence.entity.DetallePago;
import com.spring.boot.carro.circuito_manejo.persistence.entity.DetallePagoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetallePagoRepository extends JpaRepository<DetallePago, DetallePagoId> {
    // Listar todas las cuotas de un pago
    List<DetallePago> findByPagoId(Long pagoId);

    // Buscar una cuota específica
    Optional<DetallePago> findByPagoIdAndNumeroCuota(Long pagoId, Integer numeroCuota);
}
