package com.spring.boot.carro.circuito_manejo.persistence.projection;

import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;

import java.time.LocalDateTime;

public interface HorarioOcupadoProjection {
    Long getIdReserva();
    LocalDateTime getInicio();
    LocalDateTime getFin();
    Long getIdPago();
    Long getIdVehiculo();
    EstadoReservaEnum getEstado();
    String getNombre();
    String getApellido();
    String getPlacaVehiculo();
    Integer getMinutosReservados();
}
