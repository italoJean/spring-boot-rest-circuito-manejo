package com.spring.boot.carro.circuito_manejo.util.rabbit.consumer;


//@Component
//@RequiredArgsConstructor
public class ReservaStateConsumer {
/*
    private final ReservaRepository reservaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final EventoReservaRepository eventoReservaRepository;
//    private final NotificacionService notificacionService; // Asumimos un servicio de notificación

    // Escucha la única cola que recibe todos los mensajes expirados (DLQ)
    @RabbitListener(queues = "cola.procesamiento.reserva")
    public void handleReservaEvent(ReservaEstadoEvent event) {

        switch (event.getTipoEvento()) {
            case AVISO_INICIO:
//                handleAvisoInicio(event);
                break;
            case INICIAR_RESERVA:
                handleIniciarReserva(event);
                break;
            case FINALIZAR_RESERVA:
                handleFinalizarReserva(event);
                break;
            default:
                System.err.println("Tipo de evento desconocido: " + event.getTipoEvento());
        }
    }

    // -----------------------------------------------
    // LÓGICA 1: ENVÍO DE AVISO (30 minutos antes)
    // -----------------------------------------------
//    private void handleAvisoInicio(ReservaEstadoEvent event) {
//        reservaRepository.findById(event.getReservaId()).ifPresent(reserva -> {
//            // El usuario debe ser notificado
//            notificacionService.enviarNotificacion(
//                    reserva.getUsuario(),
//                    "Su reserva comienza en 30 minutos.",
//                    TipoNotificacion.EMAIL
//            );
//            System.out.println("🔔 AVISO enviado para Reserva " + reserva.getId());
//        });
//    }

    // -----------------------------------------------
    // LÓGICA 2: INICIO DE RESERVA (10:00 AM)
    // -----------------------------------------------
    private void handleIniciarReserva(ReservaEstadoEvent event) {
        reservaRepository.findById(event.getReservaId()).ifPresent(reserva -> {
            if (reserva.getEstado() == EstadoReservaEnum.RESERVADO) {
                reserva.setEstado(EstadoReservaEnum.EN_PROGRESO);
                reservaRepository.save(reserva);
                System.out.println("🔄 Reserva " + reserva.getId() + " cambió a EN_PROGRESO.");
            }
        });
    }

    // -----------------------------------------------
    // LÓGICA 3: FINALIZACIÓN DE RESERVA (12:00 PM)
    // -----------------------------------------------
    private void handleFinalizarReserva(ReservaEstadoEvent event) {
        reservaRepository.findById(event.getReservaId()).ifPresent(reserva -> {
            if (reserva.getEstado() == EstadoReservaEnum.EN_PROGRESO) {

                reserva.setEstado(EstadoReservaEnum.FINALIZADO);
                reserva.setActivo(false);

                // ... (Lógica de cálculo de minutos, registro de EventoReserva, etc. - Idéntica a su antiguo scheduler)

                // Liberar vehículo
                vehiculoRepository.findById(event.getVehiculoId()).ifPresent(vehiculo -> {
                    vehiculo.setEstado(EstadoVehiculosEnum.DISPONIBLE);
                    vehiculo.setActivo(true);
                    vehiculoRepository.save(vehiculo);
                });

                reservaRepository.save(reserva);
                System.out.println("✅ Reserva " + reserva.getId() + " FINALIZADA y vehículo liberado.");
            }
        });
    }*/
}