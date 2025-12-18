package com.spring.boot.carro.circuito_manejo.persistence.repository;

import com.spring.boot.carro.circuito_manejo.persistence.entity.EventoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoReservaRepository extends JpaRepository<EventoReserva, Long> {

    @Query("SELECT COALESCE(SUM(c.minutosAfectados),0) FROM EventoReserva c WHERE c.pago.id = :pagoId")
    Integer sumMinutosAfectadosByPago(Long pagoId);

    List<EventoReserva> findByReservaIdOrderByFechaRegistroAsc(Long reservaId);

    @Query("SELECT COUNT(e) FROM EventoReserva e WHERE e.reserva.id = :reservaId AND e.tipo = 'REPROGRAMADA'")
    long countReprogramaciones(Long reservaId);
}