package com.spring.boot.carro.circuito_manejo.presentation.dto.reserva;

import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoResumenDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.vehiculo.VehiculoResumenDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DetalleReservaResponseDTO {
    private Long id;
    private PagoResumenDTO pago;
    private VehiculoResumenDTO vehiculo;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaFin;
    private EstadoReservaEnum estado;

}
