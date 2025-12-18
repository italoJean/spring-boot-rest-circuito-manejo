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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaFinJob implements Job {

    private final ReservaRepository reservaRepository;
    private final EventoReservaRepository eventoReservaRepository;
    private final VehiculoRepository vehiculoRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {

        // 1. OBTENER DATOS
        Long reservaId = context.getMergedJobDataMap().getLong("reservaId");

        // 2. BUSCAR ENTIDAD
        Reserva reserva = reservaRepository.findById(reservaId)
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
                .detalle("Reserva finalizada por Quartz")// Reserva finalizada automáticamente por el sistema.
                .tipo(TipoEventoReservaEnum.FINALIZADO)
                .fechaRegistro(LocalDateTime.now())
                .build();

        eventoReservaRepository.save(evento);

        // 7. LIBERACIÓN DE RECURSOS (VEHÍCULO)
        // Actualiza el estado del vehículo asociado para que vuelva a estar disponible.
        Vehiculo vehiculo = reserva.getVehiculo();
        vehiculo.setEstado(EstadoVehiculosEnum.DISPONIBLE);
        vehiculoRepository.save(vehiculo);

        // 8. GUARDAR CAMBIOS FINALES DE RESERVA
        reservaRepository.save(reserva);
        log.info("🔥 JOB ACTIVADO: Reserva Fin - ID: {}. Estado cambiado a FINALIZADO.", reservaId);
    }
}

