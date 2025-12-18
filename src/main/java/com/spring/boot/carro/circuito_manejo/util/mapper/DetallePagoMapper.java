package com.spring.boot.carro.circuito_manejo.util.mapper;


import com.spring.boot.carro.circuito_manejo.persistence.entity.DetallePago;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle.CuotaResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DetallePagoMapper {

    CuotaResponseDTO toDto(DetallePago detalle);
    List<CuotaResponseDTO> toDtoList(List<DetallePago> detalles);
}
