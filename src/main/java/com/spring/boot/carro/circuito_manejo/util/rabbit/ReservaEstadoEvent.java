package com.spring.boot.carro.circuito_manejo.util.rabbit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservaEstadoEvent {
    private Long reservaId;
    private Long vehiculoId; // Para liberar el vehículo
    private TipoEvento tipoEvento; // INICIAR_RESERVA o FINALIZAR_RESERVA
    private LocalDateTime fechaLimite; // Solo informativo
}
