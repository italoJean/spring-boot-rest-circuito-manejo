package com.spring.boot.carro.circuito_manejo.service.implementation;

import com.spring.boot.carro.circuito_manejo.persistence.entity.*;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoVehiculosEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoEventoReservaEnum;
import com.spring.boot.carro.circuito_manejo.persistence.projection.HorarioOcupadoProjection;
import com.spring.boot.carro.circuito_manejo.persistence.repository.*;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.evento.*;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.ReservaMinutosDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.*;
import com.spring.boot.carro.circuito_manejo.service.exception.BusinessException;
import com.spring.boot.carro.circuito_manejo.service.exception.NotFoundException;
import com.spring.boot.carro.circuito_manejo.service.interfaces.ReservaService;
import com.spring.boot.carro.circuito_manejo.service.scheduler.ReservaJobSchedulerService;
import com.spring.boot.carro.circuito_manejo.util.mapper.ReservaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final ReservaMapper reservaMapper;
    private final VehiculoRepository vehiculoRepository;
    private final EventoReservaRepository eventoReservaRepository;
    private final ReservaJobSchedulerService reservaJobSchedulerService;
    private final String NOT_FOUND_MSG = "No se encontró ";
    private final String PAGO = "pago";
    private final String VEHICULO = "vehículo";
    private final String RESERVA = "reserva";

    //  CONSTANTES DE NEGOCIO
    private final int MAX_RESERVAS_SIMULTANEAS = 4;//eran 8
    private final int MAX_REPROGRAMACIONES_PERMITIDAS = 2;
    private final int TOLERANCIA_MINUTOS = 1;

    private final int MIN_RESERVA_MINUTOS = 60;        // mínimo 1 hora
    private final int MAX_RESERVA_MINUTOS = 300;    // máximo 5 horas
    private final int MIN_ANTICIPACION_MINUTOS = 1;//eran 5
    private final int MAX_ANTICIPACION_DIAS = 30;

    /**
     * Reglas de negocio:
     * - El pago debe estar ACTIVO o PENDIENTE para poder reservar
     * - Debe tener minutos disponibles.
     * - La fecha de reserva debe ser futura y cumplir límites de tiempo.
     * - El mismo usuario no puede tener dos reservas el mismo horario.
     * - Ocho vehiculos maximos por mismo horario.
     * - La reserva se registra con estado RESERVADO.
     * -Se reserva si el vehiculo esta disponible
     */
    @Transactional
    @Override
    public ReservaResponseDTO crearReserva(ReservaRequestDTO dto) {

        // 1. VALIDACIONES DEL DTO Y FECHA/DURACIÓN
        validarDatosGeneralesReserva(dto);

        // Obtener y validar Pago
        Pago pago = pagoRepository.findById(dto.getPagoId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + PAGO));

        validarPagoParaReserva(pago);

        // 2. VALIDAR MINUTOS DISPONIBLES EN EL PAQUETE
        Integer disponibles = obtenerMinutosDisponibles(pago);

        // Debe haber minutos disponibles para cubrir lo solicitado.
        if (disponibles < dto.getMinutosReservados()) {
            throw new BusinessException("No tienes minutos suficientes. Disponibles: " + disponibles + " min.");
        }

        // Contar reservas existentes para este pago, Si el contador es 0, es la primera reserva de este paquete.
        boolean esPrimeraReserva = reservaRepository.countReservasByPagoId(pago.getId()) == 0;

        // 3. LÓGICA DINÁMICA DEL MÍNIMO
        if (esPrimeraReserva) {
            // PARA LA PRIMERA RESERVA: Debe cumplir el mínimo (60 min).
            if (dto.getMinutosReservados() < MIN_RESERVA_MINUTOS) {
                throw new BusinessException("La PRIMERA reserva para un paquete debe ser de al menos " + MIN_RESERVA_MINUTOS + " minutos.");
            }
        }else {
            //  PARA RESERVAS SUBSECUENTES (2da en adelante)

            Integer residual = disponibles - dto.getMinutosReservados();

            if (disponibles < MIN_RESERVA_MINUTOS) {

                // A. CASO SALDO HUÉRFANO INICIAL (< 60 min, ej: 40 min). Obligamos a liquidar.
                // Si la cantidad reservada es menor que la disponible, significa que NO está liquidando el total.
                if (dto.getMinutosReservados() < disponibles) {
                    throw new BusinessException("El saldo restante (" + disponibles + " min) es menor al mínimo (" + MIN_RESERVA_MINUTOS + " min). Debes reservar la totalidad (" + disponibles + " min) para liquidar el paquete.");
                }
            } else {
                // B. CASO SALDO SUFICIENTE (>= 60 min, ej: 80, 140, 180 min). Aplicamos reglas de mínimo y huérfano.

                // B1. La reserva DEBE ser al menos de 60 minutos.
                if (dto.getMinutosReservados() < MIN_RESERVA_MINUTOS) {
                    throw new BusinessException("La duración de la reserva debe ser de al menos " + MIN_RESERVA_MINUTOS + " minutos.");
                }

                // B2. VALIDACIÓN CLAVE: EVITAR SALDO HUÉRFANO RESIDUAL.
                // El residuo no puede ser > 0 Y < 60 min.
                if (residual > 0 && residual < MIN_RESERVA_MINUTOS) {
                    throw new BusinessException("La reserva de " + dto.getMinutosReservados() + " minutos dejaría un saldo de " + residual + " minutos. Esto no está permitido. Debes reservar la totalidad (" + disponibles + " min) o dejar al menos " + MIN_RESERVA_MINUTOS + " minutos restantes.");

                }
            }
        }


        // Obtener y validar Vehículo
        Vehiculo vehiculo = vehiculoRepository.findById(dto.getVehiculoId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + VEHICULO));

        // Solo validamos que NO esté en mantenimiento
        validarVehiculoParaReserva(vehiculo);

        LocalDateTime inicio = dto.getFechaReserva();
        LocalDateTime fin = inicio.plusMinutes(dto.getMinutosReservados());

        // 4. VALIDACIONES DE CRUCE DE HORARIOS Y DISPONIBILIDAD

        // Validar cruce de horario ESPECÍFICO para este Vehículo.
        // Si la consulta devuelve > 0, significa que existe una reserva que se solapa.
        // Usamos 0L como ID a excluir ya que es una nueva reserva.
        long reservasVehiculo = reservaRepository.countCrucesVehiculo(
                0L, // ID de reserva a excluir (0L para una nueva reserva)
                vehiculo.getId(),
                inicio,
                fin
        );

        if (reservasVehiculo > 0) {
            throw new BusinessException("El vehículo seleccionado ya tiene una reserva en ese horario (" + inicio.toLocalTime() + " - " + fin.toLocalTime() + ").");
        }

        // Validar cliente no tenga otra reserva en el mismo horario
        Long clienteId = pago.getUsuario().getId();
        long reservasCliente = reservaRepository.countReservasClienteEnMismoHorario(clienteId, inicio, fin);

        if (reservasCliente > 0) {
            throw new BusinessException("Ya tienes una reserva que se cruza con este horario.");
        }

        // Validar máximo de vehículos simultáneos en ese intervalo
        long reservasTotales = reservaRepository.countReservasEnMismoHorario(inicio, fin);

        if (reservasTotales >= MAX_RESERVAS_SIMULTANEAS) {
            throw new BusinessException("No hay disponibilidad. Máximo " + MAX_RESERVAS_SIMULTANEAS + " vehículos por horario.");
        }

        // 4. CREAR Y PERSISTIR RESERVA Y EVENTO

        // Crear la reserva
        Reserva reserva = reservaMapper.toEntity(dto, pago, vehiculo);
        reserva.setEstado(EstadoReservaEnum.RESERVADO);
        reserva.setFechaRegistro(LocalDateTime.now());
        reserva.setFechaFin(fin);
        reserva.setActivo(true);

        reserva = reservaRepository.save(reserva);

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! se añadio
// Programar eventos temporales
        reservaJobSchedulerService.programarJobsReserva(reserva);

        // Registrar evento inicial
        EventoReserva eventoInicial = EventoReserva.builder()
                .pago(pago)
                .reserva(reserva)
                .minutosReservadosAntes(0)
                .minutosUsados(0)
                .minutosDevueltos(0)
                .minutosAfectados(dto.getMinutosReservados())
                .detalle("Reserva creada")
                .tipo(TipoEventoReservaEnum.RESERVA)
                .fechaRegistro(LocalDateTime.now())
                .build();

        eventoReservaRepository.save(eventoInicial);

        // Marcar vehiculo como reservado
        vehiculo.setEstado(EstadoVehiculosEnum.RESERVADO);
        vehiculoRepository.save(vehiculo);

        log.info("Reserva creada id={} pagoId={} vehiculoId={}", reserva.getId(), pago.getId(), vehiculo.getId());
        return reservaMapper.toResponse(reserva);
    }


    @Override
    public List<ReservaResponseDTO> listar() {
        return reservaRepository.findByActivoTrue()
                .stream().map(reservaMapper::toResponse).toList();
    }

    @Override
    public List<HorarioOcupadoDTO> listarCalendario() {
        return reservaRepository.findByActivoTrue()
                .stream().map(reservaMapper::toResponseHorarioOcupadoDTO).toList();
    }

    @Override
    public List<HorarioOcupadoDTO>  listarHorariosOcupados(Long vehiculoId) {
        if (!vehiculoRepository.existsById(vehiculoId)){
            throw new NotFoundException("Vehículo no encontrado");
        }

        List<HorarioOcupadoProjection> data=
                reservaRepository.findHorariosOcupadosByVehiculo(vehiculoId);

        return  data.stream()
                .map(h -> new HorarioOcupadoDTO(h.getIdReserva(),h.getInicio(), h.getFin(),h.getIdPago(),h.getIdVehiculo(),h.getEstado(),h.getNombre(),h.getApellido(),h.getPlacaVehiculo(),h.getMinutosReservados()))
                .toList();
    }

    @Override
    public List<HorarioOcupadoDTO> obtenerHorariosCliente(Long clienteId) {
        List<HorarioOcupadoProjection> data=reservaRepository.findHorariosOcupadosPorCliente(clienteId);
        return  data.stream()
                .map(c->new HorarioOcupadoDTO(c.getIdReserva(),c.getInicio(),c.getFin(),c.getIdPago(),c.getIdVehiculo(),c.getEstado(),c.getNombre(),c.getApellido(),c.getPlacaVehiculo(),c.getMinutosReservados())).toList();
    }


    /**
     * Registra una incidencia, calcula el tiempo real usado y devuelve la diferencia al saldo.
     */
    @Transactional
    @Override
    public ReservaResponseDTO registrarIncidencia(Long reservaId, IncidenciaRequestDTO incidenciaRequestDTO) {

        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() != EstadoReservaEnum.EN_PROGRESO) {
            throw new BusinessException("Solo reservas EN_PROGRESO pueden registrar incidencias.");
        }

        LocalDateTime ahora = LocalDateTime.now();

        // 1. CÁLCULO DE MINUTOS FINALES
        long minutosTranscurridos = Duration.between(reserva.getFechaReserva(), ahora).toMinutes();

        // Aplicar tolerancia (usamos max(0) para asegurar que no se reste de más)
        long minutosUsadosCalc = Math.max(0, minutosTranscurridos - TOLERANCIA_MINUTOS);

        int minutosReservadosAntes = reserva.getMinutosReservados();

        // Los minutos usados reales nunca superarán los minutos originales reservados
        int minutosUsados = (int) Math.min(minutosUsadosCalc, minutosReservadosAntes);

        int devueltos = minutosReservadosAntes - minutosUsados;
        int minutosAfectados = -devueltos; // El impacto en el saldo es la devolución

        // 2. REGISTRAR EVENTO (Si hay devolución)
        if (devueltos > 0) {
            EventoReserva incidencia = EventoReserva.builder()
                    .pago(reserva.getPago())
                    .reserva(reserva)
                    .minutosReservadosAntes(minutosReservadosAntes)
                    .minutosUsados(minutosUsados)
                    .minutosDevueltos(devueltos)
                    .minutosAfectados(minutosAfectados)
//                    .detalle("Incidencia: " + detalle)
                    .detalle("Incidencia registrada: " + incidenciaRequestDTO.getDetalle() + " | Devolución: " + devueltos + " min.")
                    .tipo(TipoEventoReservaEnum.INCIDENCIA)
                    .fechaRegistro(ahora)
                    .build();

            eventoReservaRepository.save(incidencia);
        }else {
            //OJO ESTO SE AGREGO
            // Si no hay devolución, se registra la incidencia sin afectar saldo, solo para auditoría.
            EventoReserva incidencia = EventoReserva.builder()
                    .pago(reserva.getPago())
                    .reserva(reserva)
                    .minutosReservadosAntes(minutosReservadosAntes)
                    .minutosUsados(minutosUsados)
                    .minutosDevueltos(0)
                    .minutosAfectados(0)
                    .detalle("Incidencia sin devolución de saldo: " + incidenciaRequestDTO.getDetalle())
                    .tipo(TipoEventoReservaEnum.INCIDENCIA)
                    .fechaRegistro(ahora)
                    .build();
            eventoReservaRepository.save(incidencia);
        }

        // 3. ACTUALIZAR RESERVA Y LIBERAR VEHÍCULO
        reserva.setMinutosReservados(minutosUsados); // La reserva se actualiza a su duración final real
        reserva.setEstado(EstadoReservaEnum.INCIDENCIA);//aca lo cambio finalizado estaba
        reserva.setActivo(false);
        reservaRepository.save(reserva);

        // 4. LIBERACIÓN INTELIGENTE DEL VEHÍCULO
        // Pasamos el ID de la reserva actual para que la consulta la ignore
        gestionarLiberacionVehiculo(reserva.getVehiculo(), reserva.getId());

        log.info("Incidencia registrada reservaId={} usados={} devueltos={}", reservaId, minutosUsados, devueltos);
        return reservaMapper.toResponse(reserva);
    }

    /**
     * Reprograma una reserva existente (cambio de fecha, duración o vehículo).
     * Aplica validaciones de cruce de horario y ajustes de saldo.
     */
    @Transactional
    public ReservaResponseDTO reprogramarReserva(Long reservaId, ReprogramacionRequestDTO dto) {

        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() != EstadoReservaEnum.RESERVADO) {
            throw new BusinessException("Solo reservas en estado RESERVADO pueden reprogramarse.");
        }

        // 1. VALIDACIÓN DE DATOS (Duración y Fechas)
        validarDatosReprogramacion(dto);

        // Datos nuevos
        LocalDateTime inicioNuevo = dto.getNuevaFecha();
        LocalDateTime finNuevo = inicioNuevo.plusMinutes(dto.getMinutosReservados());
        Long clienteId = reserva.getPago().getUsuario().getId();

        // Si la fecha, la duración y el vehículo son idénticos, no hay reprogramación.
        boolean esMismoHorario = reserva.getFechaReserva().equals(inicioNuevo) &&
                reserva.getMinutosReservados().equals(dto.getMinutosReservados());

        boolean esMismoVehiculo = reserva.getVehiculo().getId().equals(dto.getVehiculoId());
        if (esMismoHorario && esMismoVehiculo) {
            throw new BusinessException("La nueva programación es idéntica a la actual. No se requiere reprogramación.");
        }

        // Validar límite de reprogramaciones (máximo 2)
        long totalReprog = eventoReservaRepository.countReprogramaciones(reservaId);
        if (totalReprog >= MAX_REPROGRAMACIONES_PERMITIDAS) {
            throw new BusinessException("Solo se permiten " + MAX_REPROGRAMACIONES_PERMITIDAS + " reprogramaciones por reserva.");
        }

        // 2. VALIDAR VEHÍCULO NUEVO Y CRUCES
        Vehiculo vehiculoAnterior = reserva.getVehiculo();
        // Carga el vehículo nuevo (puede ser el mismo)
        Vehiculo vehiculoNuevo = vehiculoRepository.findById(dto.getVehiculoId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + " vehículo con id: " + dto.getVehiculoId()));

        // Validar el estado del vehículo
        validarVehiculoParaReserva(vehiculoNuevo);

        // VALIDAR QUE EL VEHICULO NUEVO NO ESTÉ OCUPADO POR OTRAS RESERVAS
        long reservasVehiculo = reservaRepository.countCrucesVehiculo(reservaId, dto.getVehiculoId(), inicioNuevo, finNuevo);

        if (reservasVehiculo > 0) {
            throw new BusinessException("El vehículo seleccionado ya tiene una reserva en ese horario (" + inicioNuevo.toLocalTime() + " - " +finNuevo.toLocalTime() + ").");
        }

        // 3. VALIDAR CRUCE DEL CLIENTE (EXCLUYENDO LA RESERVA ACTUAL)
        long reservasCliente = reservaRepository.countCrucesClienteExcluyendoActual(reservaId, clienteId, inicioNuevo, finNuevo);

        if (reservasCliente > 0) {
            throw new BusinessException("Ya tienes otra reserva que se cruza con ese horario.");
        }

        // 4. VALIDAR LIMITE DE RESERVAS SIMULTÁNEAS (CAPACIDAD TOTAL)
        long reservasTotales = reservaRepository.countCrucesHorarioExcluyendoActual(reservaId, inicioNuevo, finNuevo);

        if (reservasTotales >= MAX_RESERVAS_SIMULTANEAS) {
            throw new BusinessException("No hay disponibilidad. Máximo " + MAX_RESERVAS_SIMULTANEAS + " reservas simultáneas para ese horario.");
        }

        // 5. CÁLCULO DE MINUTOS AFECTADOS Y VALIDACIÓN DE SALDO
        int minutosActuales = reserva.getMinutosReservados();
        int minutosNuevos = dto.getMinutosReservados();

        int diferencia = minutosNuevos - minutosActuales; // + si aumenta, - si reduce
/* o jo ya no iria
        int totalAfectados = eventoReservaRepository.sumMinutosAfectadosByPago(reserva.getPago().getId());

        if (diferencia > 0 && totalAfectados + diferencia > reserva.getPago().getPaquete().getDuracionMinutos()) {
            throw new BusinessException("No tienes suficientes minutos disponibles.");
        }*/


        if (diferencia > 0) {
            // Si la duración aumenta, validar si el saldo restante del paquete cubre la diferencia.
            Integer disponibles = obtenerMinutosDisponibles(reserva.getPago());
            if (diferencia > disponibles) {
                throw new BusinessException("No tienes saldo suficiente para incrementar la reserva. Saldo disponible: " + disponibles + " min. Diferencia requerida: " + diferencia + " min.");
            }
        }
        // Nota: Si la duración se reduce, la devolución de saldo se maneja en el evento.


        // 1️⃣ ELIMINAR JOBS ANTERIORES
        reservaJobSchedulerService.eliminarJobsReserva(reserva.getId());

        // 6. ACTUALIZAR RESERVA
        LocalDateTime fechaAnterior = reserva.getFechaReserva();
        Long vehiculoAntesId = vehiculoAnterior.getId();

        reserva.setFechaReserva(inicioNuevo);
        reserva.setFechaFin(finNuevo);
        reserva.setMinutosReservados(minutosNuevos);
        reserva.setVehiculo(vehiculoNuevo);

        reservaRepository.save(reserva);

        // 🔁 Reprogramar jobs Quartz
        reservaJobSchedulerService.programarJobsReserva(reserva);

        // 7. ACTUALIZAR ESTADO DEL VEHÍCULO
        if (!esMismoVehiculo) {
            // ESCENARIO A: Cambió de vehículo.
            // El anterior debe liberarse inteligentemente (ver si alguien más lo usa).
            gestionarLiberacionVehiculo(vehiculoAnterior, reserva.getId());

            // El nuevo debe quedar marcado como RESERVADO.
            vehiculoNuevo.setEstado(EstadoVehiculosEnum.RESERVADO);
            vehiculoRepository.save(vehiculoNuevo);
        } else {
            // ESCENARIO B: Sigue con el mismo vehículo.
            // Solo nos aseguramos que esté en estado RESERVADO (por si acaso).
            if (vehiculoNuevo.getEstado() == EstadoVehiculosEnum.DISPONIBLE) {
                vehiculoNuevo.setEstado(EstadoVehiculosEnum.RESERVADO);
                vehiculoRepository.save(vehiculoNuevo);
            }
        }

        /*
        if (!vehiculoAntesId.equals(dto.getVehiculoId())) {
            // Si el vehículo cambió, liberar el anterior y reservar el nuevo
            liberarVehiculoPorId(vehiculoAntesId);
            vehiculoRepository.actualizarEstadoVehiculo(vehiculoNuevo.getId(), EstadoVehiculosEnum.RESERVADO);
        } else {
            // Si el vehículo NO cambió, solo aseguramos que el estado sigue siendo RESERVADO
//            vehiculoRepository.actualizarEstadoVehiculo(vehiculoNuevo.getId(), EstadoVehiculosEnum.RESERVADO); OJOOO
            // Si el vehículo NO cambió, NO hacemos nada (ya está reservado y evita una query/update innecesario).
        }*/

        // 8. REGISTRAR EVENTO
        Integer minutosUsadosEvento = 0;
        Integer minutosDevueltosEvento = 0;
        String detalleAdicional = "";

        if (diferencia > 0) {
            minutosUsadosEvento = diferencia;
//            detalleAdicional = " (+" + diferencia + " min)";
            detalleAdicional = " (Aumento: +" + diferencia + " min)";
        } else if (diferencia < 0) {
            minutosDevueltosEvento = Math.abs(diferencia);
            detalleAdicional = " (Devolución: -" + minutosDevueltosEvento + " min)";
//            detalleAdicional = " (-" + minutosDevueltosEvento + " min)";
        }

        String detalleVehiculo = vehiculoAntesId.equals(dto.getVehiculoId())
                ? ""
                : " | Vehículo: antes " + vehiculoAntesId + " ahora " + dto.getVehiculoId();

        String detalleCompleto = "Reprogramada: antes " + fechaAnterior +
                " ahora " + inicioNuevo + detalleAdicional + detalleVehiculo;

        EventoReserva evento = EventoReserva.builder()
                .pago(reserva.getPago())
                .reserva(reserva)
                .minutosReservadosAntes(minutosActuales)
                .minutosUsados(minutosUsadosEvento)
                .minutosDevueltos(minutosDevueltosEvento)
                .minutosAfectados(diferencia) // Diferencia es el cambio neto en el saldo
                .detalle(detalleCompleto)
                .numeroReprogramacion((int) totalReprog + 1)
                .tipo(TipoEventoReservaEnum.REPROGRAMADA)
                .fechaRegistro(LocalDateTime.now())
                .build();

        eventoReservaRepository.save(evento);

        log.info("Reserva reprogramada id={} antes={} ahora={}", reserva.getId(), fechaAnterior, inicioNuevo);

        return reservaMapper.toResponse(reserva);
    }

    /**
     * Cancela una reserva que aún no ha comenzado y devuelve la totalidad de los minutos.
     */
    @Transactional
    @Override
    public void cancelarReserva(Long reservaId) {

        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() == EstadoReservaEnum.CANCELADO) {
            throw new BusinessException("La reserva ya se encuentra cancelada.");
        }

        if (reserva.getEstado() != EstadoReservaEnum.RESERVADO) {
            throw new BusinessException("Solo reservas en estado RESERVADO pueden cancelarse. Estado actual: " + reserva.getEstado());
        }

        //  Una reserva solo puede cancelarse si aún no ha comenzado.
        if (reserva.getFechaReserva().isBefore(LocalDateTime.now())) {
            throw new BusinessException("La reserva ya ha comenzado y no puede cancelarse. Utiliza el proceso de INCIDENCIA");
        }

        // 1. ACTUALIZAR RESERVA
        reserva.setEstado(EstadoReservaEnum.CANCELADO);
        reserva.setActivo(false);
        reservaRepository.save(reserva);

        // 2. REGISTRAR EVENTO DE DEVOLUCIÓN TOTAL
        int minutosADevolver = reserva.getMinutosReservados();

        EventoReserva eventoCancelacion = EventoReserva.builder()
                .pago(reserva.getPago())
                .reserva(reserva)
                .minutosReservadosAntes(minutosADevolver)
                .minutosUsados(0)
                .minutosDevueltos(minutosADevolver)
                .minutosAfectados(-minutosADevolver) // Devolución total
                .detalle("Devolución total por cancelación a tiempo.")
                .tipo(TipoEventoReservaEnum.CANCELADO)
                .fechaRegistro(LocalDateTime.now())
                .build();
        eventoReservaRepository.save(eventoCancelacion);

        // 3. LIBERAR VEHÍCULO
//        liberarVehiculo(reserva.getVehiculo());
        gestionarLiberacionVehiculo(reserva.getVehiculo(), reserva.getId());

        log.info("Reserva cancelada id={} devolución={}", reservaId, reserva.getMinutosReservados());
    }


    /**
     * Proporciona un resumen del consumo de minutos para un pago específico,
     * incluyendo el saldo actual y los detalles de las reservas.
     */
    @Override
    @Transactional(readOnly = true)
    public PagoMinutosDTO detalleMinutos(Long pagoId) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + PAGO));

        // El saldo disponible se calcula siempre a partir de los eventos.
        Integer consumidos = obtenerMinutosConsumidos(pagoId);

//        Integer consumidos = eventoReservaRepository.sumMinutosAfectadosByPago(pagoId);
//        if (consumidos == null) consumidos = 0; ya no vaa

        int minutosTotales = pago.getPaquete().getDuracionMinutos();
        int disponibles = minutosTotales - consumidos;

        List<ReservaMinutosDTO> reservas = reservaRepository.findByPagoId(pagoId)
                .stream()
                .map(r -> {

                    // Carga el historial de eventos para cada reserva
                    List<EventoReserva> consumos = eventoReservaRepository.findByReservaIdOrderByFechaRegistroAsc(r.getId());
                    List<IncidenciaDTO> incidencias = consumos.stream()
                            .map(c -> IncidenciaDTO.builder()
                                    .minutosReservadosAntes(c.getMinutosReservadosAntes())
                                    .minutosUsados(c.getMinutosUsados())
                                    .minutosDevueltos(c.getMinutosDevueltos())
                                    .minutosAfectados(c.getMinutosAfectados())
                                    .detalle(c.getDetalle())
                                    .fechaRegistro(c.getFechaRegistro())
                                    .tipo(c.getTipo())
                                    .build()
                            ).toList();

                    return ReservaMinutosDTO.builder()
                            .reservaId(r.getId())
                            .fechaReserva(r.getFechaReserva())
                            .fechaFin(r.getFechaFin())
                            .minutosReservados(r.getMinutosReservados())
                            .estado(r.getEstado())
                            .detalle(incidencias.isEmpty() ? Collections.emptyList() : incidencias)
                            .build();
                }).toList();

        return PagoMinutosDTO.builder()
                .pagoId(pagoId)
                .minutosTotalesPaquete(minutosTotales)
                .minutosConsumidos(consumidos)
                .minutosDisponibles(Math.max(disponibles, 0))
                .reservas(reservas)
                .build();
    }

    @Override
    public DetalleReservaResponseDTO detalleReserva(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));

        return reservaMapper.toDetalleDTO(reserva);
    }

    /**
     * Valida si el pago puede crear reservas.
     */
    private void validarPagoParaReserva(Pago pago) {
        if (pago.getEstado() == EstadoPagoEnum.CANCELADO)
            throw new BusinessException("El pago está cancelado.");
        if (!(pago.getEstado() == EstadoPagoEnum.PAGADO || pago.getEstado() == EstadoPagoEnum.PENDIENTE))
            throw new BusinessException("Estado de pago no permite reservas: " + pago.getEstado());
    }

    /**
     * Valida el estado del vehículo antes de usarlo en una reserva.
     */
    private void validarVehiculoParaReserva(Vehiculo v) {
        if (v.getEstado() == EstadoVehiculosEnum.MANTENIMIENTO)
            throw new BusinessException("Vehículo en mantenimiento.");
    }

    /**
     * Obtiene la suma total de minutos consumidos/devueltos del paquete (a partir de EventoReserva).
     */
    private Integer obtenerMinutosConsumidos(Long pagoId) {
        Integer consumidos = eventoReservaRepository.sumMinutosAfectadosByPago(pagoId);
        return consumidos == null ? 0 : consumidos;
    }

    /**
     * Calcula los minutos disponibles del paquete asociado al pago,
     */
    private Integer obtenerMinutosDisponibles(Pago pago) {
        if (pago == null) throw new IllegalArgumentException("Pago no puede ser null");
/*
        // Minutos consumidos o devueltos (suma acumulada)
        Integer consumidos = eventoReservaRepository.sumMinutosAfectadosByPago(pago.getId());
        if (consumidos == null) consumidos = 0;

        int minutosTotales = pago.getPaquete().getDuracionMinutos();
        int disponibles = minutosTotales - consumidos;

        return Math.max(disponibles, 0);*/


        Integer consumidos = obtenerMinutosConsumidos(pago.getId());
        int minutosTotales = pago.getPaquete().getDuracionMinutos();
        int disponibles = minutosTotales - consumidos;

        return Math.max(disponibles, 0);
    }

    /**
     * Obtiene una reserva o lanza excepción.
     */
    private Reserva obtenerReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + RESERVA));
    }

    /**
     * Cambia el estado del vehículo a DISPONIBLE.
     */
    private void liberarVehiculo(Vehiculo v) {
        if (v.getEstado() != EstadoVehiculosEnum.DISPONIBLE) {
            v.setEstado(EstadoVehiculosEnum.DISPONIBLE);
            vehiculoRepository.save(v);
        }
    }

    /**
     * Cambia el estado del vehículo a DISPONIBLE usando el ID
     */
    private void liberarVehiculoPorId(Long vehiculoId) {
        vehiculoRepository.actualizarEstadoVehiculo(vehiculoId, EstadoVehiculosEnum.DISPONIBLE);
    }

    /**
     * Verifica si existen otras reservas para decidir el estado final del vehículo.
     */
    private void gestionarLiberacionVehiculo(Vehiculo vehiculo, Long reservaActualId) {
        // Usamos el método que creamos anteriormente en el repositorio
        boolean tieneMasReservas = reservaRepository.existsOtrasReservasActivas(vehiculo.getId(), reservaActualId);

        if (tieneMasReservas) {
            // Si hay alguien más esperando (RESERVADO) o usando (EN_PROGRESO)
            vehiculo.setEstado(EstadoVehiculosEnum.RESERVADO);
            log.info("Vehículo ID: {} permanece RESERVADO por otras reservas pendientes.", vehiculo.getId());
        } else {
            // Si no hay nadie más, queda libre
            vehiculo.setEstado(EstadoVehiculosEnum.DISPONIBLE);
            log.info("Vehículo ID: {} ahora está DISPONIBLE.", vehiculo.getId());
        }

        vehiculoRepository.save(vehiculo);
    }

    /**
     * Valida la duración máxima y límites de anticipación para la creación de una reserva.
     * NO valida el mínimo de 60 minutos, lo cual se deja a la lógica de consumo.
     */
    private void validarDatosGeneralesReserva(ReservaRequestDTO dto) {

        // 1. VALIDAR DURACIÓN MÁXIMA (300 MIN)
        // Se valida solo que sea positivo y no exceda el máximo.
        if (dto.getMinutosReservados() == null || dto.getMinutosReservados() <= 0 || dto.getMinutosReservados() > MAX_RESERVA_MINUTOS) {
            throw new BusinessException("La duración de la reserva debe ser positiva y no puede exceder los " + MAX_RESERVA_MINUTOS + " minutos.");
        }

        // 2. VALIDAR FECHA DE RESERVA FUTURA CON MARGEN MÍNIMO
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime ahoraConMargen = ahora.plusMinutes(MIN_ANTICIPACION_MINUTOS);

        if (dto.getFechaReserva().isBefore(ahoraConMargen)) {
            throw new BusinessException("La reserva debe ser al menos con " + MIN_ANTICIPACION_MINUTOS + " minutos de anticipación.");
        }

        // 3. VALIDAR LÍMITE DE ANTICIPACIÓN MÁXIMA
        if (dto.getFechaReserva().isAfter(ahora.plusDays(MAX_ANTICIPACION_DIAS))) {
            throw new BusinessException("No se permiten reservas con más de " + MAX_ANTICIPACION_DIAS + " días de anticipación.");
        }
    }

    /*
    private void validarDatosReserva(ReservaRequestDTO dto) {

        // 1. VALIDAR DURACIÓN MÍNIMA Y MÁXIMA
        if (dto.getMinutosReservados() == null || dto.getMinutosReservados() < MIN_RESERVA_MINUTOS || dto.getMinutosReservados() > MAX_RESERVA_MINUTOS) {

            throw new BusinessException("La duración de la reserva debe ser entre " + MIN_RESERVA_MINUTOS + " y " + MAX_RESERVA_MINUTOS + " minutos.");
        }

        // 2. VALIDAR FECHA DE RESERVA FUTURA CON MARGEN MÍNIMO
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime ahoraConMargen = ahora.plusMinutes(MIN_ANTICIPACION_MINUTOS);

        if (dto.getFechaReserva().isBefore(ahoraConMargen)) {
            throw new BusinessException("La reserva debe ser al menos con " + MIN_ANTICIPACION_MINUTOS + " minutos de anticipación.");
        }

        // 3. VALIDAR LÍMITE DE ANTICIPACIÓN MÁXIMA
        if (dto.getFechaReserva().isAfter(ahora.plusDays(MAX_ANTICIPACION_DIAS))) {
            throw new BusinessException("No se permiten reservas con más de " + MAX_ANTICIPACION_DIAS + " días de anticipación.");
        }
    }*/


    /**
     * Valida la duración y límites de anticipación para la reprogramación.
     */
    private void validarDatosReprogramacion(ReprogramacionRequestDTO dto) {

        // 1. VALIDAR DURACIÓN MÍNIMA Y MÁXIMA
        if (dto.getMinutosReservados() == null || dto.getMinutosReservados() < MIN_RESERVA_MINUTOS || dto.getMinutosReservados() > MAX_RESERVA_MINUTOS) {
            // En reprogramación, SIEMPRE debe cumplir el mínimo normal.
            throw new BusinessException("La duración de la reserva debe ser entre " + MIN_RESERVA_MINUTOS + " y " + MAX_RESERVA_MINUTOS + " minutos.");
        }

        // 2. VALIDAR FECHA DE RESERVA FUTURA CON MARGEN MÍNIMO
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime ahoraConMargen = ahora.plusMinutes(MIN_ANTICIPACION_MINUTOS);

        if (dto.getNuevaFecha().isBefore(ahoraConMargen)) {
            throw new BusinessException("La reserva debe ser al menos con " + MIN_ANTICIPACION_MINUTOS + " minutos de anticipación.");
        }

        // 3. VALIDAR LÍMITE DE ANTICIPACIÓN MÁXIMA
        if (dto.getNuevaFecha().isAfter(ahora.plusDays(MAX_ANTICIPACION_DIAS))) {
            throw new BusinessException("No se permiten reservas con más de " + MAX_ANTICIPACION_DIAS + " días de anticipación.");
        }
    }


}

