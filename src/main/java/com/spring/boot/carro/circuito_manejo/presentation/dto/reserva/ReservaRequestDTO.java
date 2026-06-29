package com.spring.boot.carro.circuito_manejo.presentation.dto.reserva;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "Solicitud para crear una reserva de una sesion de manejo.")
public class ReservaRequestDTO {

    @Schema(description = "Identificador del pago que habilita la reserva.", example = "25")
    @NotNull
    private Long pagoId;

    @Schema(description = "Identificador del vehiculo asignado a la sesion.", example = "4")
    @NotNull
    private Long vehiculoId;

    @Schema(description = "Fecha y hora de inicio de la reserva.", example = "2026-05-28T09:00:00")
    @NotNull
    private LocalDateTime fechaReserva;

    @Schema(description = "Duracion de la sesion en minutos.", example = "60")
    @NotNull
    @Positive
    private Integer minutosReservados;
}
