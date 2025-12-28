package com.spring.boot.carro.circuito_manejo.presentation.controller;

import com.spring.boot.carro.circuito_manejo.persistence.repository.PagoRepository;
import com.spring.boot.carro.circuito_manejo.persistence.repository.ReservaRepository;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reporte.AnalisisRetencionDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reporte.EstadoPagoDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reporte.UsoVehiculoDTO;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final IReporteService reporteService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        // Obtenemos el mes y año actual automáticamente del sistema
        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();

        int anioActual = hoy.getYear();

        Map<String, Object> response = new HashMap<>();

        // Agregamos la rentabilidad real calculada por el service
        response.put("rentabilidadPaquetes", reporteService.calcularRentabilidadMes(mesActual, anioActual));

        // Uso de Vehículos (Total Reservas + Suma de Minutos) del mes actual
        List<UsoVehiculoDTO> usoVehiculos = reservaRepository.obtenerUsoVehiculosMesActual(mesActual, anioActual);
        response.put("usoVehiculos", usoVehiculos);

        List<EstadoPagoDTO> estadoPagos=pagoRepository.obtenerResumenClientesPorEstado(mesActual, anioActual);
        response.put("estadosPago", estadoPagos);

        List<AnalisisRetencionDTO> retencionClientes=reporteService.prepararRetencionHistorica();
        response.put("retencionClientes", retencionClientes);

        // Información adicional para el título del Dashboard
        response.put("mesNombre", hoy.getMonth().name());
        response.put("anio", anioActual);

        return ResponseEntity.ok(response);
    }
}