package com.spring.boot.carro.circuito_manejo.util.websocket;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.presentation.dto.ReservaNotificationDTO;
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

    public void notificarReservaProxima(Reserva reserva) {

        Long usuarioId = reserva.getPago().getUsuario().getId();
        String emailUsuario = reserva.getPago().getUsuario().getEmail();

        // 1️⃣ NOTIFICACIÓN WEBSOCKET (Dashboard interno)
        ReservaNotificationDTO dto = new ReservaNotificationDTO(
                reserva.getId(),
                "⏰ Tu reserva inicia en 10 minutos",
                reserva.getFechaReserva()
        );

        messagingTemplate.convertAndSend(
                "/topic/reservas/" + usuarioId,
                dto
        );

        log.info("🔔 WebSocket enviado | Reserva={} Usuario={}", reserva.getId(), usuarioId);

        // 2️⃣ NOTIFICACIÓN POR CORREO
        enviarCorreoReservaProxima(reserva, emailUsuario);
    }

    private void enviarCorreoReservaProxima(Reserva reserva, String emailUsuario) {

        String asunto = "⏰ Tu reserva inicia en 10 minutos";

        String cuerpo = """
                Hola %s,

                Te recordamos que tu reserva iniciará en 10 minutos.

                📅 Fecha y hora: %s
                🚗 Vehículo: %s %s
                ⏱️ Duración: %d minutos

                Por favor llega puntual.

                Saludos,
                Sistema de Reservas
                """.formatted(
                reserva.getPago().getUsuario().getNombre(),
                reserva.getFechaReserva(),
                reserva.getVehiculo().getMarca(),
                reserva.getVehiculo().getModelo(),
                reserva.getMinutosReservados()
        );

        emailService.sendEmail(
                new String[]{emailUsuario},
                asunto,
                cuerpo
        );

        log.info("📧 Email enviado | Reserva={} Email={}", reserva.getId(), emailUsuario);
    }
/*
    public void notificarReservaProxima(Reserva reserva) {

        ReservaNotificationDTO dto = new ReservaNotificationDTO(
                reserva.getId(),
                "⏰ Tu reserva inicia en 10 minutos",
                reserva.getFechaReserva()
        );

        messagingTemplate.convertAndSend(
                "/topic/reservas/" + reserva.getPago().getUsuario().getId(),
                dto
        );
        log.info("🔔 Notificación enviada para Reserva ID: {} al usuario ID: {}",
                reserva.getId(),
                reserva.getPago().getUsuario().getId(),
                reserva.getFechaReserva()
        );

    }*/
}
