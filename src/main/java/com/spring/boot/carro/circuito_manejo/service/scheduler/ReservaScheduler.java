package com.spring.boot.carro.circuito_manejo.service.scheduler;

import com.spring.boot.carro.circuito_manejo.persistence.entity.EventoReserva;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Vehiculo;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoVehiculosEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoEventoReservaEnum;
import com.spring.boot.carro.circuito_manejo.persistence.repository.EventoReservaRepository;
import com.spring.boot.carro.circuito_manejo.persistence.repository.ReservaRepository;
import com.spring.boot.carro.circuito_manejo.persistence.repository.VehiculoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


//@Service
//@RequiredArgsConstructor
public class ReservaScheduler {

    //private final ReservaRepository reservaRepository;
   // private final VehiculoRepository vehiculoRepository;
   // private final EventoReservaRepository eventoReservaRepository;

    /**
     * Tareas cada 1 minuto:
     * - RESERVADO → EN_PROGRESO si ya inició
     * - EN_PROGRESO → FINALIZADO si ya terminó
     * - Registrar evento FINALIZADO al completar
     */
 //   @Scheduled(cron = "0 */1 * * * *")  // Cada 1 minuto
   // @Transactional
    /*
    public void actualizarEstadosReservas() {

        LocalDateTime ahora = LocalDateTime.now();

        // 1) RESERVAS QUE YA INICIARON → EN_PROGRESO
        List<Reserva> iniciar = reservaRepository
                .findByEstadoAndFechaReservaBefore(EstadoReservaEnum.RESERVADO, ahora);

        iniciar.forEach(r -> {
                    r.setEstado(EstadoReservaEnum.EN_PROGRESO);
                }
        );
        reservaRepository.saveAll(iniciar);


        // 2) RESERVAS QUE YA FINALIZARON → FINALIZADO
        List<Reserva> finalizar = reservaRepository
                .findByEstadoAndFechaFinBefore(EstadoReservaEnum.EN_PROGRESO, ahora);

        finalizar.forEach(reserva -> {
            reserva.setEstado(EstadoReservaEnum.FINALIZADO);
            reserva.setActivo(false);


            // Calcular minutos usados reales
            long minutosUsados = Duration.between(
                    reserva.getFechaReserva(),
                    reserva.getFechaFin()
            ).toMinutes();

            // Registrar evento FINALIZADO
            EventoReserva evento = EventoReserva.builder()
                    .pago(reserva.getPago())
                    .reserva(reserva)
                    .minutosReservadosAntes(reserva.getMinutosReservados())
                    .minutosUsados((int) minutosUsados)
                    .minutosDevueltos(0)
                    .minutosAfectados(0)
                    .detalle("Reserva finalizada automáticamente por el sistema.")
                    .tipo(TipoEventoReservaEnum.FINALIZADO)
                    .fechaRegistro(ahora)
                    .build();

            eventoReservaRepository.save(evento);


            // Liberar vehículo automáticamente
            Vehiculo vehiculo = reserva.getVehiculo();
            vehiculo.setEstado(EstadoVehiculosEnum.DISPONIBLE);
            vehiculo.setActivo(true);
            vehiculoRepository.save(vehiculo);
        });

        reservaRepository.saveAll(finalizar);
    }*/
}
