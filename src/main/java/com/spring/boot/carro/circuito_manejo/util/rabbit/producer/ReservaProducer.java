package com.spring.boot.carro.circuito_manejo.util.rabbit.producer;

//@Slf4j
//@Service
//@RequiredArgsConstructor
public class ReservaProducer {
/*
    private final RabbitTemplate rabbitTemplate;

    private static final String DLX_EXCHANGE = "dlx.reserva.exchange";
    private static final String DL_ROUTING_KEY = "reserva.event"; // Usamos una sola clave

    public void programarCambiosDeEstado(Reserva reserva) {
        LocalDateTime ahora = LocalDateTime.now();

        // ----------------------------------------------------
        // 1. EVENTO DE AVISO (30 minutos antes de fechaReserva)
        // ----------------------------------------------------
        LocalDateTime avisoTime = reserva.getFechaReserva().minusMinutes(30);
        long delayAvisoMs = Duration.between(ahora, avisoTime).toMillis();

        if (delayAvisoMs > 0) {
            ReservaEstadoEvent avisoEvent = new ReservaEstadoEvent(
                    reserva.getId(),
                    reserva.getVehiculo().getId(),
                    TipoEvento.AVISO_INICIO,
                    avisoTime
            );
            enviarMensajeRetrasado(avisoEvent, delayAvisoMs);
        }

        // ----------------------------------------------------
        // 2. EVENTO DE INICIO (En fechaReserva)
        // ----------------------------------------------------
        long delayIniciarMs = Duration.between(ahora, reserva.getFechaReserva()).toMillis();

        if (delayIniciarMs > 0) {
            ReservaEstadoEvent iniciarEvent = new ReservaEstadoEvent(
                    reserva.getId(),
                    reserva.getVehiculo().getId(),
                    TipoEvento.INICIAR_RESERVA,
                    reserva.getFechaReserva()
            );
            enviarMensajeRetrasado(iniciarEvent, delayIniciarMs);
        }

        // ----------------------------------------------------
        // 3. EVENTO DE FINALIZACIÓN (En fechaFin)
        // ----------------------------------------------------
        long delayFinalizarMs = Duration.between(ahora, reserva.getFechaFin()).toMillis();

        if (delayFinalizarMs > 0) {
            ReservaEstadoEvent finalizarEvent = new ReservaEstadoEvent(
                    reserva.getId(),
                    reserva.getVehiculo().getId(),
                    TipoEvento.FINALIZAR_RESERVA,
                    reserva.getFechaFin()
            );
            enviarMensajeRetrasado(finalizarEvent, delayFinalizarMs);
        }
    }

    private void enviarMensajeRetrasado(ReservaEstadoEvent event, long delayMs) {
        // Usa el Plugin de Delayed Message Exchange (requiere la configuración en RabbitMQConfig)
        rabbitTemplate.convertAndSend(
                DLX_EXCHANGE, DL_ROUTING_KEY, event, m -> {
                    m.getMessageProperties().setDelayLong(delayMs);
                    return m;
                }
        );
        System.out.println("Programado evento " + event.getTipoEvento() + " para Reserva " + event.getReservaId() + " con " + delayMs + " ms de retraso.");
        log.info("Programado evento " + event.getTipoEvento() + " para Reserva " + event.getReservaId() + " con " + delayMs + " ms de retraso.");
    }

 */
}