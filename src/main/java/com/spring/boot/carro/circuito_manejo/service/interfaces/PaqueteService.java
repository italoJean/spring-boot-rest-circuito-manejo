package com.spring.boot.carro.circuito_manejo.service.interfaces;

import com.spring.boot.carro.circuito_manejo.presentation.dto.paquete.PaqueteDTO;

import java.util.List;

public interface PaqueteService {

    public List<PaqueteDTO> listar();

    public PaqueteDTO obtenerPorId(Long id);

    public PaqueteDTO crear(PaqueteDTO paqueteDTO);

    public PaqueteDTO actualizar(Long id, PaqueteDTO paqueteDTO);

    public void eliminar(Long id);

    public boolean tienePagosAsociados(Long id) ;
}
