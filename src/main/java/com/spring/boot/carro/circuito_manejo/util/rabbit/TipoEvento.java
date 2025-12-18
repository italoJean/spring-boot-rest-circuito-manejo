package com.spring.boot.carro.circuito_manejo.util.rabbit;

public enum TipoEvento {
    AVISO_INICIO,        // Nuevo tipo: para la notificación
    INICIAR_RESERVA,     // Para el cambio de RESERVADO a EN_PROGRESO
    FINALIZAR_RESERVA    // Para el cambio de EN_PROGRESO a FINALIZADO
}
