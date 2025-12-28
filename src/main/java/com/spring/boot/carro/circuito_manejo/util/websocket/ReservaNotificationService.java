package com.spring.boot.carro.circuito_manejo.util.websocket;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.evento.ReservaNotificationDTO;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final IEmailService emailService;

    // --- EL MÉTODO QUE FALTA: Notificación 1 hora antes (Solo Email al Cliente) ---
    public void notificarPorCorreo(String emailDestino, Reserva reserva) {
        String asunto = "📅 Recordatorio: Tu reserva inicia en 1 hora";

        String cuerpo = String.format(
                """
                        Hola %s %s,

                        Te recordamos que tu reserva está programada para hoy a las %s.
                        🚗 Vehículo: %s %s
                        ⏱️ Duración: %d minutos.

                        ¡Te esperamos!

                        Saludos,
                        Sistema de Reservas""",
                reserva.getPago().getUsuario().getNombre(),              // %s (Hola...)
                reserva.getPago().getUsuario().getApellido(),
                reserva.getFechaReserva().toLocalTime().toString(),      // %s (Hora)
                reserva.getVehiculo().getMarca(),                        // %s (Marca)
                reserva.getVehiculo().getModelo(),                       // %s (Modelo)
                reserva.getMinutosReservados()                           // %d (Minutos)
        );

        emailService.sendEmail(new String[]{emailDestino}, asunto, cuerpo);
        log.info("📧 Email de recordatorio (1h) enviado a: {}", emailDestino);
    }

    // --- Notificación 10 minutos antes (Gmail + WebSocket) ---
    public void notificarUsuarioGmail(String emailGmail, Reserva reserva) {
        // 1. Email a la cuenta de Gmail logueada
        String asunto = "⏰ ¡Iniciamos en 10 minutos!";
        String cuerpo = "El sistema te informa que la reserva #" + reserva.getId() + " comenzará muy pronto.";

        emailService.sendEmail(new String[]{emailGmail}, asunto, cuerpo);

        // 2. WebSocket para el Dashboard
        Long usuarioId = reserva.getPago().getUsuario().getId();
        ReservaNotificationDTO dto = new ReservaNotificationDTO(
                reserva.getId(),
                " Tu reserva inicia en 10 min",
                reserva.getFechaReserva()
        );

        messagingTemplate.convertAndSend("/topic/reservas/" + emailGmail, dto);
        log.info("🔔 Alerta 10min enviada por Email a {} y WebSocket a usuario {}", emailGmail, usuarioId);
    }
}


