package com.spring.boot.carro.circuito_manejo.service.scheduler;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.service.scheduler.job.ReservaFinJob;
import com.spring.boot.carro.circuito_manejo.service.scheduler.job.ReservaInicioJob;
import com.spring.boot.carro.circuito_manejo.service.scheduler.job.ReservaNotificacionJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaJobSchedulerService {

    //  Interfaz principal para programar, desprogramar y gestionar Jobs.
    private final Scheduler scheduler;

    public void programarJobsReserva(Reserva reserva) {
    // Método de alto nivel que se llama desde el Servicio principal de su aplicación.
        programarInicio(reserva);
        programarFin(reserva);
        programarNotificacion(reserva);
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

            scheduler.deleteJob(JobKey.jobKey("notif-reserva-" + reservaId));
        } catch (SchedulerException e) {
            // 3. MANEJO DE ERRORES
            // Si hay un error de comunicación con el Scheduler (ej. fallo de la DB de Quartz),
            // se captura la excepción y se relanza como una RuntimeException (o se maneja el error de forma específica).
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

        /*
        // 2. CREACIÓN DEL TRIGGER (ACTUALIZADO)
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("inicio-reserva-trigger-" + reserva.getId()) // Buena práctica darle un nombre al Trigger
                .startAt(Date.from(reserva.getFechaReserva()
                        .atZone(ZoneId.systemDefault()).toInstant()))
                // AÑADIDO: Configuración del horario simple
                .withSchedule(simpleSchedule()
                        .withRepeatCount(0) // Ejecutar solo una vez (fundamental)
                        .withIntervalInSeconds(0) // Sin intervalo de repetición
                        // CLAVE: Instrucción de Misfire: si la hora de inicio pasó, dispáralo inmediatamente.
                        // Luego, se considera completado y Quartz lo borra (comportamiento correcto para un evento único).
                        .withMisfireHandlingInstructionFireNow())
                .forJob(job) // Se enlaza al Job creado arriba
                .build();*/

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

        /*
        // 2. CREACIÓN DEL TRIGGER (ACTUALIZADO)
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("fin-reserva-trigger-" + reserva.getId()) // Buena práctica darle un nombre al Trigger
                .startAt(Date.from(reserva.getFechaFin()
                        .atZone(ZoneId.systemDefault()).toInstant()))
                // AÑADIDO: Configuración del horario simple
                .withSchedule(simpleSchedule()
                        .withRepeatCount(0) // Ejecutar solo una vez
                        .withIntervalInSeconds(0)
                        // CLAVE: Instrucción de Misfire (Disparar inmediatamente si está atrasado)
                        .withMisfireHandlingInstructionFireNow())
                .forJob(job)
                .build();
        */

        // 3. PROGRAMACIÓN
        schedule(job, trigger);
    }

    private void programarNotificacion(Reserva reserva) {

        LocalDateTime notificacionTime = reserva.getFechaReserva().minusMinutes(10);

        // Evitar fechas pasadas
        if (notificacionTime.isBefore(LocalDateTime.now())) {
            log.info("⏱ No se programa notificación: faltan menos de 10 minutos para la reserva {}", reserva.getId());
            return;
        }

        JobDetail job = JobBuilder.newJob(ReservaNotificacionJob.class)
                .withIdentity("notif-reserva-" + reserva.getId())
                .usingJobData("reservaId", reserva.getId())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(notificacionTime
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .build();

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
