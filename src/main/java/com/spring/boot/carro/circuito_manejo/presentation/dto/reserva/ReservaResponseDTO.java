package com.spring.boot.carro.circuito_manejo.presentation.dto.reserva;

import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta resumida de una reserva registrada en el sistema.")
public class ReservaResponseDTO {

    @Schema(description = "Identificador de la reserva.", example = "18")
    private Long id;

    @Schema(description = "Numero de boleta asociado al pago que genero la reserva.", example = "BOL-00025")
    private String numeroBoleta;

    @Schema(description = "Nombre del cliente.", example = "Italo")
    private String nombre;

    @Schema(description = "Apellido del cliente.", example = "Carlos")
    private String apellido;

    @Schema(description = "Placa del vehiculo asignado.", example = "ABC-123")
    private String placaVehiculo;

    @Schema(description = "Modelo del vehiculo asignado.", example = "Yaris")
    private String modeloVehiculo;

    @Schema(description = "Fecha y hora de inicio de la reserva.", example = "2026-05-28T09:00:00")
    private LocalDateTime fechaReserva;

    @Schema(description = "Fecha y hora de fin de la reserva.", example = "2026-05-28T10:00:00")
    private LocalDateTime fechaFin;

    @Schema(description = "Duracion reservada en minutos.", example = "60")
    private Integer minutosReservados;

    @Schema(description = "Estado actual de la reserva.", example = "PROGRAMADA")
    private EstadoReservaEnum estado;
}
