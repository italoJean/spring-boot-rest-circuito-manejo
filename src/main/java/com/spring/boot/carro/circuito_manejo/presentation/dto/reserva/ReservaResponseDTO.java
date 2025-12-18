package com.spring.boot.carro.circuito_manejo.presentation.dto.reserva;


import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaResponseDTO {
    private Long id;
    private String numeroBoleta;
    private String nombre;
    private String apellido;
    private String placaVehiculo;
    private String modeloVehiculo;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaFin;
    private Integer minutosReservados;
    private EstadoReservaEnum estado;
}
