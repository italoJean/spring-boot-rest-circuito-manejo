package com.spring.boot.carro.circuito_manejo.service.scheduler.job;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.circuito_manejo.persistence.repository.ReservaRepository;
import com.spring.boot.carro.circuito_manejo.util.websocket.ReservaNotificationService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ReservaNotificacionJob implements Job {

    @Autowired
    private   ReservaRepository reservaRepository;

    @Autowired
    private  ReservaNotificationService notificationService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        Long reservaId = data.getLong("reservaId");
        String tipo = data.getString("tipoNotif");

// 1. Buscamos la reserva con JOIN FETCH para evitar LazyInitializationException
        Reserva reserva = reservaRepository.findByIdCompleto(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + reservaId));

        if (reserva.getEstado() == EstadoReservaEnum.RESERVADO) {
            if ("CLIENTE".equals(tipo)) {
                // Notificar al dueño de la reserva (1 hora antes)
                notificationService.notificarPorCorreo(reserva.getPago().getUsuario().getEmail(), reserva);
            } else if ("GMAIL_USUARIO".equals(tipo)) { //no va eso solo el ELSE
                // Notificar a la persona de Gmail + WebSocket (10 min antes)
                // Usamos emailExtra que capturamos en el controlador
                notificationService.notificarUsuarioGmail(reserva.getEmailCreador(), reserva);
            }
        }
    }
}


