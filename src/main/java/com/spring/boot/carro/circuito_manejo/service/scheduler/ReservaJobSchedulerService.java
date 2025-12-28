package com.spring.boot.carro.circuito_manejo.service.scheduler;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.service.scheduler.job.ReservaFinJob;
import com.spring.boot.carro.circuito_manejo.service.scheduler.job.ReservaInicioJob;
import com.spring.boot.carro.circuito_manejo.service.scheduler.job.ReservaNotificacionJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class ReservaJobSchedulerService {

    //  Interfaz principal para programar, desprogramar y gestionar Jobs.
    @Autowired
    private  Scheduler scheduler;


    public void programarJobsReserva(Reserva reserva) {
        eliminarJobsReserva(reserva.getId()); // Siempre limpiar antes de programar

        programarInicio(reserva);
        programarFin(reserva);
        programarNotificacion(reserva, 60, "CLIENTE"); // Notificación 1h
        programarNotificacion(reserva, 10, "GMAIL_USUARIO"); // Notificación 10m
    }

    private void programarNotificacion(Reserva reserva, int minutos, String tipo) {
        LocalDateTime tiempo = reserva.getFechaReserva().minusMinutes(minutos);
        if (tiempo.isBefore(LocalDateTime.now())) return;

        JobDetail job = JobBuilder.newJob(ReservaNotificacionJob.class)
                .withIdentity("notif-" + tipo + "-" + reserva.getId())
                .usingJobData("reservaId", reserva.getId()) // SOLO pasamos el ID
                .usingJobData("tipoNotif", tipo)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(tiempo
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        schedule(job, trigger);
    }

    public void eliminarJobsReserva(Long reservaId) {
        try {
            // 1. ELIMINAR JOB DE INICIO DE RESERVA (RESERVADO -> EN PROGRESO)
            // Método central de Quartz para eliminar un Job Detail y todos sus Triggers asociados de la base de datos.
            scheduler.deleteJob(JobKey.jobKey("inicio-reserva-" + reservaId));

            // 2. ELIMINAR JOB DE FIN DE RESERVA (EN PROGRESO -> FINALIZADO)
            scheduler.deleteJob(JobKey.jobKey("fin-reserva-" + reservaId));
            // **JobKey.jobKey()**: Crea un identificador único para el Job que se desea eliminar.
            // La clave debe coincidir exactamente con el 'identity' usado al programar el Job.

//            scheduler.deleteJob(JobKey.jobKey("notif-reserva-" + reservaId)); esto ya no va y va lo debaajo?=
            scheduler.deleteJob(JobKey.jobKey("notif-CLIENTE-" + reservaId));
        scheduler.deleteJob(JobKey.jobKey("notif-GMAIL_USUARIO-" + reservaId));
            log.info("🧹 BD Quartz limpia para reserva ID: {}", reservaId);
        } catch (SchedulerException e) {
            // 3. MANEJO DE ERRORES
            // Si hay un error de comunicación con el Scheduler (ej. fallo de la DB de Quartz),
            // se captura la excepción y se relanza como una RuntimeException (o se maneja el error de forma específica).
            log.error("Error al eliminar jobs de la reserva {}", reservaId, e);
            throw new RuntimeException("Error eliminando jobs Quartz", e);
        }
    }

    private void programarInicio(Reserva reserva) {

        // 1. CREACIÓN DEL JOB DETAIL
        JobDetail job = JobBuilder.newJob(ReservaInicioJob.class)   // Asocia el JobDetail con la clase que contiene la lógica.
                .withIdentity("inicio-reserva-" + reserva.getId())  // Le da un identificador único al Job (para Quartz).
                .usingJobData("reservaId", reserva.getId()) // Pasa el ID de la reserva como parámetro al Job.
                .build();

        // 2. CREACIÓN DEL TRIGGER (CUÁNDO EJECUTAR)
        Trigger trigger = TriggerBuilder.newTrigger()
                // Indica que el Job debe comenzar exactamente a la hora de inicio de la reserva.
                .startAt(Date.from(reserva.getFechaReserva()
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .build();



        // 3. PROGRAMACIÓN
        schedule(job, trigger);
    }

    private void programarFin(Reserva reserva) {
        // Lógica similar para programar la finalización, pero usando la hora de finalización de la reserva.

        // 1. CREACIÓN DEL JOB DETAIL
        JobDetail job = JobBuilder.newJob(ReservaFinJob.class)  // Asocia con la clase de lógica FinReservaJob.
                .withIdentity("fin-reserva-" + reserva.getId())
                .usingJobData("reservaId", reserva.getId())
                .build();

        // 2. CREACIÓN DEL TRIGGER (CUÁNDO EJECUTAR)
        Trigger trigger = TriggerBuilder.newTrigger()
                // Indica que el Job debe comenzar exactamente a la hora de fin de la reserva.
                .startAt(Date.from(reserva.getFechaFin()
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .build();


        // 3. PROGRAMACIÓN
        schedule(job, trigger);
    }


    private void schedule(JobDetail job, Trigger trigger) {
        try {
            // Llama al Scheduler para guardar el Job y el Trigger en la base de datos.
            // Una vez aquí, Quartz se encarga de monitorear el tiempo y disparar el Job.
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            // Manejo de errores: Si Quartz no puede programar (ej. DB no disponible), lanza una excepción.
            throw new RuntimeException("Error programando job Quartz", e);
        }
    }
}
