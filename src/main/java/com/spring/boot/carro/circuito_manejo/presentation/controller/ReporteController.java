package com.spring.boot.carro.circuito_manejo.presentation.controller;

import com.spring.boot.carro.circuito_manejo.configuration.app.OpenApiConfig;
import com.spring.boot.carro.circuito_manejo.persistence.repository.PagoRepository;
import com.spring.boot.carro.circuito_manejo.persistence.repository.ReservaRepository;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reporte.AnalisisRetencionDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reporte.EstadoPagoDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reporte.UsoVehiculoDTO;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Reportes", description = "Endpoints orientados a dashboard y analitica operativa.")
@SecurityRequirement(name = OpenApiConfig.SESSION_AUTH_SCHEME)
public class ReporteController {

    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final IReporteService reporteService;

    @GetMapping("/dashboard-stats")
    @Operation(summary = "Obtener estadisticas del dashboard", description = "Devuelve rentabilidad, uso de vehiculos, estados de pago y retencion historica para el panel principal.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estadisticas obtenidas correctamente"),
            @ApiResponse(responseCode = "401", description = "Sesion no autenticada")
    })
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();

        Map<String, Object> response = new HashMap<>();
        response.put("rentabilidadPaquetes", reporteService.calcularRentabilidadMes(mesActual, anioActual));

        List<UsoVehiculoDTO> usoVehiculos = reservaRepository.obtenerUsoVehiculosMesActual(mesActual, anioActual);
        response.put("usoVehiculos", usoVehiculos);

        List<EstadoPagoDTO> estadoPagos = pagoRepository.obtenerResumenClientesPorEstado(mesActual, anioActual);
        response.put("estadosPago", estadoPagos);

        List<AnalisisRetencionDTO> retencionClientes = reporteService.prepararRetencionHistorica();
        response.put("retencionClientes", retencionClientes);

        response.put("mesNombre", hoy.getMonth().name());
        response.put("anio", anioActual);

        return ResponseEntity.ok(response);
    }
}
