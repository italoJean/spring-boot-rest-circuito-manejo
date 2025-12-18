package com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.evento;

import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;

import java.time.LocalDateTime;

public record HorarioOcupadoDTO(
        Long idReserva,
        LocalDateTime inicio,
        LocalDateTime fin,
        Long idPago,
        Long idVehiculo,
        EstadoReservaEnum estado,
        String nombre,
        String apellido,
        String placaVehiculo,
        Integer minutosReservados
) {}
