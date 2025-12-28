package com.spring.boot.carro.circuito_manejo.service.scheduler.job;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.circuito_manejo.persistence.repository.ReservaRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component // Marca la clase como un componente de Spring, permitiendo la inyección de dependencias.
@NoArgsConstructor
@Slf4j
public class ReservaInicioJob implements Job {

    @Autowired
    private  ReservaRepository reservaRepository;

    @Override
    @Transactional// Asegura que todas las operaciones de BD sean atómicas (o se ejecutan todas, o ninguna).
    public void execute(JobExecutionContext context) {
        // Este método se ejecuta cuando el Trigger asociado a este Job se dispara.

        // 1. OBTENER DATOS DEL JOB
        // Recupera el 'reservaId' que fue pasado al Job cuando fue programado por el Scheduler.
        Long reservaId = context.getMergedJobDataMap().getLong("reservaId");

        // 2. BUSCAR ENTIDAD
        // Busca la reserva en la base de datos. Si no la encuentra, lanza una excepción (el Job fallará y Quartz lo registrará).
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow();

        // 3. LÓGICA DE TRANSICIÓN (IDEMPOTENCIA)
        // Verifica el estado actual. Esto es crucial para la robustez y evita errores
        // si el Job se dispara dos veces o si la reserva fue cancelada manualmente.
        if (reserva.getEstado() == EstadoReservaEnum.RESERVADO) {

            // 4. APLICAR CAMBIO DE ESTADO
            reserva.setEstado(EstadoReservaEnum.EN_PROGRESO);

            // 5. GUARDAR CAMBIOS
            reservaRepository.save(reserva);
            log.info("🔥 JOB ACTIVADO: Reserva Inicio - ID: {}. Estado cambiado a EN_PROGRESO.", reservaId);
            // **NOTA:** En este punto, la lógica de negocio debe programar el siguiente Job (FinReservaJob).
            // Esto se haría llamando al ReservaJobSchedulerService desde aquí o desde el servicio principal
            // inmediatamente después de esta transición.
        }
    }
}
