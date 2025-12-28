package com.spring.boot.carro.circuito_manejo.service.interfaces;

import com.spring.boot.carro.circuito_manejo.persistence.enums.MetodoPagoEnum;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.*;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle.PagoDetalleResponseDTO;

import java.util.List;

public interface IPagoService {

    public List<PagoListadoResponseDTO> listarPagos();

    public PagoDetalleResponseDTO obtenerPagoConCuotas(Long id);

    public PagoListadoResponseDTO crearPagoContado(PagoContadoRequestDTO pagoRequestDTO);

    public PagoDetalleResponseDTO crearPagoCuotas(PagoCuotasRequestDTO pagoRequestDTO);

    public void pagarCuota(Long idPago, Integer numeroCuota, MetodoPagoEnum metodoPago);

    public void suspenderPago(Long id);

}
