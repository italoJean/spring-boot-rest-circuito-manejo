package com.spring.boot.carro.circuito_manejo.service.scheduler.job;

import com.spring.boot.carro.circuito_manejo.persistence.entity.EventoReserva;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Vehiculo;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoVehiculosEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoEventoReservaEnum;
import com.spring.boot.carro.circuito_manejo.persistence.repository.EventoReservaRepository;
import com.spring.boot.carro.circuito_manejo.persistence.repository.ReservaRepository;
import com.spring.boot.carro.circuito_manejo.persistence.repository.VehiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
public class ReservaFinJob implements Job {

    @Autowired
    private  ReservaRepository reservaRepository;
    @Autowired
    private  EventoReservaRepository eventoReservaRepository;
    @Autowired
    private  VehiculoRepository vehiculoRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {

        // 1. OBTENER DATOS
        Long reservaId = context.getMergedJobDataMap().getLong("reservaId");

        // 2. BUSCAR ENTIDAD  Reserva reserva = reservaRepository.findById(reservaId)
        Reserva reserva = reservaRepository.findByIdCompleto(reservaId)
                .orElseThrow();

        // 3. LÓGICA DE TRANSICIÓN (VALIDACIÓN)
        // Solo finaliza si la reserva está EN_PROGRESO. Si ya está FINALIZADO (ej. por un usuario), se detiene.
        if (reserva.getEstado() != EstadoReservaEnum.EN_PROGRESO) return;

        // 4. APLICAR CAMBIOS DE ESTADO FINAL
        reserva.setEstado(EstadoReservaEnum.FINALIZADO);
        reserva.setActivo(false);

        // 5. CÁLCULO DE DURACIÓN
        // Calcula la duración (en minutos) utilizada para la reserva.
        long minutosUsados = Duration.between(
                reserva.getFechaReserva(),
                reserva.getFechaFin()
        ).toMinutes();

        // 6. TRAZABILIDAD (CREACIÓN DE EVENTO)
        // Registra el evento de FINALIZACIÓN para auditoría y historial.
        EventoReserva evento = EventoReserva.builder()
                .pago(reserva.getPago())
                .reserva(reserva)
                .minutosReservadosAntes(reserva.getMinutosReservados())
                .minutosUsados((int) minutosUsados)
                .minutosDevueltos(0)
                .minutosAfectados(0)
                .detalle("Reserva finalizada")// Reserva finalizada automáticamente por el sistema.
                .tipo(TipoEventoReservaEnum.FINALIZADO)
                .fechaRegistro(LocalDateTime.now())
                .build();

        eventoReservaRepository.save(evento);

        // 7. LIBERACIÓN CONDICIONAL DE RECURSOS (VEHÍCULO)
        Vehiculo vehiculo = reserva.getVehiculo();

        // Comprobar si hay otras reservas que requieran el vehículo
        boolean tieneMasReservas = reservaRepository.existsOtrasReservasActivas(vehiculo.getId(), reservaId);

        if (tieneMasReservas) {
            // Si hay más reservas, el vehículo NO se pone disponible, se mantiene RESERVADO u OCUPADO
            vehiculo.setEstado(EstadoVehiculosEnum.RESERVADO);
            log.info("ℹ️ Vehículo ID: {} continúa RESERVADO debido a otras reservas pendientes.", vehiculo.getId());
        } else {
            // Si no hay más, queda LIBRE
            vehiculo.setEstado(EstadoVehiculosEnum.DISPONIBLE);
            log.info("✅ Vehículo ID: {} ahora está DISPONIBLE.", vehiculo.getId());
        }
        vehiculoRepository.save(vehiculo);

        // 8. GUARDAR CAMBIOS FINALES DE RESERVA
        reservaRepository.save(reserva);
        log.info("🔥 JOB ACTIVADO: Reserva Fin - ID: {}. Estado cambiado a FINALIZADO.", reservaId);
    }
}

