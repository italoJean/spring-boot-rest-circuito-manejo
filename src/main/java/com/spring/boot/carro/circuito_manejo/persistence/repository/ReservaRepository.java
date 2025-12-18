package com.spring.boot.carro.circuito_manejo.persistence.repository;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Usuario;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.circuito_manejo.persistence.projection.HorarioOcupadoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByEstadoAndFechaReservaBefore(EstadoReservaEnum estado, LocalDateTime fecha);

    List<Reserva> findByEstadoAndFechaFinBefore(EstadoReservaEnum estado, LocalDateTime fecha);

    List<Reserva> findByPagoId(Long pagoId);

    /**
     * Esta consulta verifica si un cliente específico ya tiene alguna reserva activa que se cruce con el nuevo período propuesto.
     * Cuenta el número de reservas ACTIVAS que tiene un cliente específico
     * (identificado por :clienteId) cuyo intervalo de tiempo se solapa (cruza)
     * con el período definido por [:inicio, :fin].
     * * Propósito: Prevenir que un mismo usuario tenga dos reservas simultáneas.
     */
    @Query(" SELECT COUNT(r) FROM Reserva r WHERE r.pago.usuario.id = :clienteId AND r.activo = true AND ( (r.fechaReserva < :fin AND r.fechaFin > :inicio) ) ")
    long countReservasClienteEnMismoHorario(Long clienteId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Esta es una variación de la primera consulta, utilizada específicamente para la reprogramación o modificación de una reserva existente.
     * Cuenta el número de reservas ACTIVAS que tiene un cliente específico
     * (identificado por :clienteId) que se cruzan con el nuevo horario
     * [:inicio, :fin], EXCLUYENDO la reserva que se está modificando
     * (identificada por :reservaId).
     * * Propósito: Validar que, al reprogramar, el cliente no cree un cruce
     * consigo mismo usando sus otras reservas (sin contarse a sí misma).
     */
    @Query(" SELECT COUNT(r) FROM Reserva r WHERE r.id <> :reservaId AND r.pago.usuario.id = :clienteId AND r.activo = true AND (r.fechaReserva < :fin AND r.fechaFin > :inicio) ")
    long countCrucesClienteExcluyendoActual(Long reservaId, Long clienteId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Esta consulta verifica la disponibilidad general o la carga total del sistema, sin importar quién hizo la reserva.
     * Cuenta el número total de reservas ACTIVAS (de cualquier cliente)
     * cuyo intervalo de tiempo se solapa (cruza) con el período definido por
     * [:inicio, :fin].
     * * Propósito: Validar el límite máximo de reservas simultáneas permitido
     * en el sistema (ej. 4 vehículos a la vez).
     */
    @Query("  SELECT COUNT(r) FROM Reserva r WHERE r.activo = true AND (r.fechaReserva < :fin AND r.fechaFin > :inicio) ")
    long countReservasEnMismoHorario(LocalDateTime inicio, LocalDateTime fin);


    /**
     * Esta es una variación de la segunda consulta, también utilizada en el contexto de reprogramación o modificación para verificar la capacidad total.
     * Cuenta el número total de reservas ACTIVAS (de cualquier cliente)
     * cuyo intervalo de tiempo se solapa (cruza) con el nuevo horario
     * [:inicio, :fin], EXCLUYENDO la reserva que se está modificando
     * (identificada por :reservaId).
     * * Propósito: Validar que, al reprogramar, la reserva modificada no exceda
     * el límite máximo de capacidad simultánea al considerar todas las demás
     * reservas.
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.id <> :reservaId AND r.activo = true AND (r.fechaReserva < :fin AND r.fechaFin > :inicio) ")
    long countCrucesHorarioExcluyendoActual(Long reservaId, LocalDateTime inicio, LocalDateTime fin);

    List<Reserva> findByActivoTrue();

    //para reprogramar el scheduded
//    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.vehiculo.id = :vehiculoId AND r.id <> :reservaId AND r.activo = true AND r.fechaReserva < :fin AND r.fechaFin > :inicio")
//    long countCrucesVehiculoReserva(Long reservaId, Long vehiculoId, LocalDateTime inicio, LocalDateTime fin);

    long countReservasByPagoId(Long pagoId);

    // Ejemplo de consulta JPQL (o lógica de Criteria API)
    @Query("SELECT COUNT(r) FROM Reserva r " +
            "WHERE r.vehiculo.id = :vehiculoId " +
            "AND r.id != :reservaIdExcluir " +
            "AND r.estado IN ('RESERVADO', 'EN_PROGRESO') " +
            "AND ( " +
            "    (:inicio < r.fechaFin AND :fin > r.fechaReserva) " + // Lógica de superposición estándar
            ")")
    long countCrucesVehiculo(
            @Param("reservaIdExcluir") Long reservaIdExcluir,
            @Param("vehiculoId") Long vehiculoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("SELECT r.id AS idReserva, r.fechaReserva AS inicio, r.fechaFin AS fin, r.pago.id AS idPago, r.vehiculo.id AS idVehiculo, r.estado AS estado, r.pago.usuario.nombre AS nombre, r.pago.usuario.apellido AS apellido, r.vehiculo.placa AS placa, r.minutosReservados AS minutosReservados  FROM Reserva r WHERE r.vehiculo.id = :vehiculoId AND r.activo = true AND r.estado <> 'CANCELADO' ")
    List<HorarioOcupadoProjection> findHorariosOcupadosByVehiculo(Long vehiculoId);
/*
    @Query("SELECT r.id AS reservaId, r.fechaInicio AS inicio, r.fechaFin AS fin FROM Reserva r WHERE r.vehiculo.id = :vehiculoId AND r.activo = true AND r.estado IN ('RESERVADO', 'EN_PROGRESO') ")
   List<HorarioOcupadoProjection> findHorariosOcupadosPorVehiculo(Long vehiculoId);
*/

//    @Query("SELECT r.id AS idReserva, r.fechaReserva AS inicio, r.fechaFin AS fin FROM Reserva r WHERE r.pago.id = :clienteId AND r.activo = true AND r.estado IN ('RESERVADO','EN_PROGRESO')")
@Query("SELECT r.id AS idReserva, r.fechaReserva AS inicio, r.fechaFin AS fin, r.vehiculo.id AS idVehiculo, r.pago.id AS idPago, r.estado AS estado, r.pago.usuario.nombre AS nombre, r.pago.usuario.apellido AS apellido, r.vehiculo.placa AS placaVehiculo, r.minutosReservados AS minutosReservados FROM Reserva r WHERE r.pago.id = :pagoId AND r.activo = true AND r.estado IN ('RESERVADO','EN_PROGRESO')")
List<HorarioOcupadoProjection> findHorariosOcupadosPorCliente(Long pagoId);

}
