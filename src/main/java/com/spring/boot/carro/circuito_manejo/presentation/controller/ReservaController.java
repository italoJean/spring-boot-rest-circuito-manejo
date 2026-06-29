package com.spring.boot.carro.circuito_manejo.presentation.controller;

import com.spring.boot.carro.circuito_manejo.configuration.app.OpenApiConfig;
import com.spring.boot.carro.circuito_manejo.presentation.dto.ErrorDetailDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.DetalleReservaResponseDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.ReservaRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.ReservaResponseDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.evento.HorarioOcupadoDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.evento.IncidenciaRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.evento.PagoMinutosDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.evento.ReprogramacionRequestDTO;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
@Tag(name = "Reservas", description = "Gestion de reservas, calendario, reprogramaciones e incidencias.")
@SecurityRequirement(name = OpenApiConfig.SESSION_AUTH_SCHEME)
public class ReservaController {

    @Autowired
    private IReservaService reservaService;

    @PostMapping
    @Operation(summary = "Crear reserva", description = "Registra una nueva reserva asociada a un pago vigente y a un vehiculo disponible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida o regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pago o vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<ReservaResponseDTO> crearReserva(@Valid @RequestBody ReservaRequestDTO reservaRequest) {
        ReservaResponseDTO reservaCreada = reservaService.crearReserva(reservaRequest);
        return ResponseEntity.ok(reservaCreada);
    }

    @GetMapping
    @Operation(summary = "Listar reservas", description = "Devuelve todas las reservas registradas.")
    public ResponseEntity<List<ReservaResponseDTO>> findAll() {
        return ResponseEntity.ok(reservaService.listar());
    }

    @GetMapping("/calendario")
    @Operation(summary = "Obtener calendario de reservas", description = "Lista los bloques de horario ocupados para visualizar el calendario de reservas.")
    public ResponseEntity<List<HorarioOcupadoDTO>> findAllCalendario() {
        return ResponseEntity.ok(reservaService.listarCalendario());
    }

    @GetMapping("/{id}/detalle")
    @Operation(summary = "Obtener detalle de reserva", description = "Devuelve toda la informacion relevante de una reserva concreta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<DetalleReservaResponseDTO> obtenerDetalle(
            @Parameter(description = "Identificador de la reserva.", example = "18")
            @PathVariable Long id) {
        return ResponseEntity.ok(reservaService.detalleReserva(id));
    }

    @PatchMapping("/{id}/reprogramar")
    @Operation(summary = "Reprogramar reserva", description = "Actualiza la fecha u hora de una reserva manteniendo sus reglas operativas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva reprogramada correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida o conflicto de horario",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<ReservaResponseDTO> reprogramar(
            @Parameter(description = "Identificador de la reserva.", example = "18")
            @PathVariable Long id,
            @Valid @RequestBody ReprogramacionRequestDTO dto) {
        ReservaResponseDTO response = reservaService.reprogramarReserva(id, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva vigente y libera la disponibilidad asociada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reserva cancelada correctamente"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<Void> cancelar(@Parameter(description = "Identificador de la reserva.", example = "18")
                                         @PathVariable Long id) {
        reservaService.cancelarReserva(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/incidencia")
    @Operation(summary = "Registrar incidencia", description = "Registra una incidencia operativa ocurrida durante una reserva.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidencia registrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida o regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<ReservaResponseDTO> registrarIncidencia(
            @Parameter(description = "Identificador de la reserva.", example = "18")
            @PathVariable Long id,
            @RequestBody IncidenciaRequestDTO incidenciaRequestDTO) {
        ReservaResponseDTO response = reservaService.registrarIncidencia(id, incidenciaRequestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pagoId}/minutos")
    @Operation(summary = "Consultar minutos disponibles", description = "Obtiene el saldo de minutos consumidos o disponibles asociado a un pago.")
    public ResponseEntity<PagoMinutosDTO> obtenerMinutos(
            @Parameter(description = "Identificador del pago.", example = "25")
            @PathVariable Long pagoId) {
        return ResponseEntity.ok(reservaService.detalleMinutos(pagoId));
    }

    @GetMapping("/horarios")
    @Operation(summary = "Consultar horarios disponibles", description = "Devuelve los bloques de horario ocupados o restringidos para un vehiculo y un pago.")
    public ResponseEntity<List<HorarioOcupadoDTO>> listar(
            @Parameter(description = "Identificador del vehiculo a consultar.", example = "4")
            @RequestParam Long vehiculoId,
            @Parameter(description = "Identificador del pago vinculado a la reserva.", example = "25")
            @RequestParam Long pagoId) {
        return ResponseEntity.ok(reservaService.obtenerHorarios(vehiculoId, pagoId));
    }
}
