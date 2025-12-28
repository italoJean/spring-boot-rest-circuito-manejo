package com.spring.boot.carro.circuito_manejo.util.mapper;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Pago;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Reserva;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Vehiculo;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.DetalleReservaResponseDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.ReservaRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.ReservaResponseDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.evento.HorarioOcupadoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
                PagoMapper.class,
                VehiculoMapper.class
        })
public interface ReservaMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pago", source = "pago")
    @Mapping(target = "vehiculo", source = "vehiculo")
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaFin", ignore = true)
    @Mapping(target = "eventos", ignore = true)
    @Mapping(target = "activo", constant = "true")
    Reserva toEntity(ReservaRequestDTO dto, Pago pago, Vehiculo vehiculo);

    @Mapping(target = "numeroBoleta", source = "pago.numeroBoleta")
    @Mapping(target = "nombre", source = "pago.usuario.nombre")
    @Mapping(target = "apellido", source = "pago.usuario.apellido")
    @Mapping(target = "placaVehiculo", source = "vehiculo.placa")
    @Mapping(target = "modeloVehiculo", source = "vehiculo.modelo")
    ReservaResponseDTO toResponse(Reserva reserva);


    @Mapping(target = "inicio", source = "fechaReserva")
    @Mapping(target = "fin", source = "fechaFin")
    @Mapping(target = "nombre", source = "pago.usuario.nombre")
    @Mapping(target = "apellido", source = "pago.usuario.apellido")
    @Mapping(target = "placaVehiculo", source = "vehiculo.placa")
    @Mapping(target = "idVehiculo", source = "vehiculo.id")
    @Mapping(target = "idPago", source = "pago.id")
    @Mapping(target = "idReserva", source = "id")
    HorarioOcupadoDTO toResponseHorarioOcupadoDTO(Reserva reserva);

    List<ReservaResponseDTO> toResponseList(List<Reserva> reservas);

    @Mapping(target = "pago", source = "pago",qualifiedByName = "toPagoResumenDTO")
    @Mapping(target = "vehiculo", source = "vehiculo",qualifiedByName = "toVehiculoResumenDTO")
    DetalleReservaResponseDTO toDetalleDTO(Reserva reserva);

}
