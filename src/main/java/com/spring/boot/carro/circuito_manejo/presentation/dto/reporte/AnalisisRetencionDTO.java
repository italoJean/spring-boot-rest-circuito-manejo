package com.spring.boot.carro.circuito_manejo.presentation.dto.reporte;

public record AnalisisRetencionDTO(
        String categoria, // "A Tiempo", "En Riesgo", "Abandono"
        Long cantidadClientes,
        Double promedioDiasRetraso
) {}
