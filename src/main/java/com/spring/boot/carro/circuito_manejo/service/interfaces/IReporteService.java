package com.spring.boot.carro.circuito_manejo.service.interfaces;

import com.spring.boot.carro.circuito_manejo.presentation.dto.reporte.AnalisisRetencionDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reporte.RentabilidadDTO;

import java.util.List;

public interface IReporteService {

    public List<AnalisisRetencionDTO> prepararRetencionHistorica();
    public List<RentabilidadDTO> calcularRentabilidadMes(int mes, int anio);
}
