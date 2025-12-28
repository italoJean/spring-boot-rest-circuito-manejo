package com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.evento;

import java.time.LocalDateTime;

public record ReservaNotificationDTO (
        Long reservaId,
        String mensaje,
        LocalDateTime fechaInicio
){
}
