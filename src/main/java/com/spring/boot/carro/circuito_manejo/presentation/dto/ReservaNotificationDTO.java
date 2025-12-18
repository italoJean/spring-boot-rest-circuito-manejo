package com.spring.boot.carro.circuito_manejo.presentation.dto;

import java.time.LocalDateTime;

public record ReservaNotificationDTO (
        Long reservaId,
        String mensaje,
        LocalDateTime fechaInicio
){
}
