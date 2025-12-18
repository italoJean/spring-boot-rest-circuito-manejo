package com.spring.boot.carro.circuito_manejo.service.scheduler.job;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.circuito_manejo.persistence.repository.ReservaRepository;
import com.spring.boot.carro.circuito_manejo.util.websocket.ReservaNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaNotificacionJob implements Job {

    private final ReservaRepository reservaRepository;
    private final ReservaNotificationService notificationService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long reservaId = context.getMergedJobDataMap().getLong("reservaId");

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow();

        // Solo si sigue reservada
        if (reserva.getEstado() == EstadoReservaEnum.RESERVADO) {
            notificationService.notificarReservaProxima(reserva);
            log.info("🔔 JOB ACTIVADO: Notificación Reserva Próxima - ID: {}. Notificación enviada.", reservaId);
        }
    }
    }
