package com.spring.boot.carro.circuito_manejo.util.mapper;

import com.spring.boot.carro.circuito_manejo.persistence.entity.DetallePago;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Pago;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Paquete;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Usuario;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoContadoRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoCuotasRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoListadoResponseDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoResumenDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle.CuotaResponseDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle.PagoDetalleResponseDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.paquete.PaqueteDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class, PaqueteMapper.class})
public interface PagoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", source = "usuario")
    @Mapping(target = "paquete", source = "paquete")
    @Mapping(target = "numeroBoleta", ignore = true)
    @Mapping(target = "monto", source = "paquete.precioTotal")
    @Mapping(target = "tipoPago", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Pago toPagoContado(PagoContadoRequestDTO dto, Usuario usuario, Paquete paquete);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", source = "usuario")
    @Mapping(target = "paquete", source = "paquete")
    @Mapping(target = "numeroBoleta", ignore = true)
//    @Mapping(target = "numeroBoleta", expression = "java(java.util.UUID.randomUUID().toString().substring(0, 8))")
    @Mapping(target = "monto", source = "paquete.precioTotal")
    @Mapping(target = "tipoPago", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Pago toPagoCuotas(PagoCuotasRequestDTO dto, Usuario usuario, Paquete paquete);

    @Mapping(target = "nombreUsuario", source = "usuario.nombre")
    @Mapping(target = "apellidoUsuario", source = "usuario.apellido")
    @Mapping(target = "nombrePaquete", source = "paquete.nombre")
    PagoListadoResponseDTO toListadoResponse(Pago pago);

    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "toUsuarioResumenDTO")
    @Mapping(target = "paquete", source = "paquete", qualifiedByName = "toPaqueteResumenDTO")
    @Mapping(target = "cuotas", source = "detalles")
    PagoDetalleResponseDTO toDetalleResponse(Pago pago);

    CuotaResponseDTO toCuotaResponse(DetallePago detalle);

    List<PaqueteDTO> toResponseList(List<Paquete> paquetes);

    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "toUsuarioResumenDTO")
    @Mapping(target = "paquete", source = "paquete", qualifiedByName = "toPaqueteResumenDTO")
    @Named("toPagoResumenDTO")
    PagoResumenDTO toResumenDTO(Pago pago);
}