package com.spring.boot.carro.circuito_manejo.presentation.controller;

import com.spring.boot.carro.circuito_manejo.configuration.app.OpenApiConfig;
import com.spring.boot.carro.circuito_manejo.presentation.dto.ErrorDetailDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.reserva.evento.HorarioOcupadoDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.vehiculo.VehiculoDTO;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IReservaService;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IVehiculoService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehiculos")
@Tag(name = "Vehiculos", description = "Gestion de vehiculos y su disponibilidad operativa.")
@SecurityRequirement(name = OpenApiConfig.SESSION_AUTH_SCHEME)
public class VehiculoController {

    @Autowired
    private IVehiculoService vehiculoService;

    @Autowired
    private IReservaService reservaService;

    @GetMapping
    @Operation(summary = "Listar vehiculos", description = "Obtiene todos los vehiculos registrados.")
    public ResponseEntity<List<VehiculoDTO>> findAll() {
        return ResponseEntity.ok(vehiculoService.listar());
    }

    @GetMapping("/operativos")
    @Operation(summary = "Listar vehiculos operativos", description = "Devuelve solo los vehiculos en estado operativo.")
    public List<VehiculoDTO> listarVehiculosOperativos() {
        return vehiculoService.listarVehiculosOperativos();
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Listar vehiculos disponibles", description = "Devuelve los vehiculos que pueden ser asignados a nuevas reservas.")
    public ResponseEntity<List<VehiculoDTO>> findAllDisponibles() {
        return ResponseEntity.ok(vehiculoService.listarVehiculosDisponibles());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar vehiculo", description = "Actualiza la informacion de un vehiculo existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehiculo actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<VehiculoDTO> update(@RequestBody @Valid VehiculoDTO vehiculoDTO,
                                              @Parameter(description = "Identificador del vehiculo.", example = "4")
                                              @PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.actualizar(id, vehiculoDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener vehiculo por id", description = "Devuelve el detalle de un vehiculo.")
    public ResponseEntity<VehiculoDTO> findById(@Parameter(description = "Identificador del vehiculo.", example = "4")
                                                @PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar vehiculo", description = "Elimina un vehiculo del sistema.")
    public ResponseEntity<Void> delete(@Parameter(description = "Identificador del vehiculo.", example = "4")
                                       @PathVariable Long id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @Operation(summary = "Crear vehiculo", description = "Registra un nuevo vehiculo para el circuito.")
    public ResponseEntity<VehiculoDTO> create(@RequestBody @Valid VehiculoDTO vehiculoDTO,
                                              UriComponentsBuilder uriComponentsBuilder) {
        VehiculoDTO creado = vehiculoService.crear(vehiculoDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/vehiculos/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @PatchMapping("/reservar/{id}")
    @Operation(summary = "Marcar vehiculo como reservado", description = "Actualiza el estado del vehiculo para reflejar que fue asignado a una reserva.")
    public ResponseEntity<Void> reservarVehiculo(@Parameter(description = "Identificador del vehiculo.", example = "4")
                                                 @PathVariable Long id) {
        vehiculoService.marcarVehiculoComoReservado(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/liberar/{id}")
    @Operation(summary = "Liberar vehiculo", description = "Marca el vehiculo como disponible nuevamente.")
    public ResponseEntity<Void> liberarVehiculo(@Parameter(description = "Identificador del vehiculo.", example = "4")
                                                @PathVariable Long id) {
        vehiculoService.liberarVehiculo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{vehiculoId}/horarios-ocupados")
    @Operation(summary = "Consultar horarios ocupados del vehiculo", description = "Devuelve los bloques de tiempo ya reservados para un vehiculo.")
    public ResponseEntity<List<HorarioOcupadoDTO>> getHorariosOcupados(
            @Parameter(description = "Identificador del vehiculo.", example = "4")
            @PathVariable Long vehiculoId) {
        return ResponseEntity.ok(reservaService.listarHorariosOcupados(vehiculoId));
    }

    @GetMapping("/{pagoId}/horarios-ocupados-cliente")
    @Operation(summary = "Consultar horarios ocupados del cliente", description = "Devuelve horarios incompatibles para el cliente segun sus reservas previas.")
    public ResponseEntity<List<HorarioOcupadoDTO>> horariosCliente(
            @Parameter(description = "Identificador del pago asociado al cliente.", example = "25")
            @PathVariable Long pagoId) {
        return ResponseEntity.ok(reservaService.obtenerHorariosCliente(pagoId));
    }
}
