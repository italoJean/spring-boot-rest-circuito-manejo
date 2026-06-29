package com.spring.boot.carro.circuito_manejo.presentation.controller;

import com.spring.boot.carro.circuito_manejo.configuration.app.OpenApiConfig;
import com.spring.boot.carro.circuito_manejo.presentation.dto.ErrorDetailDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoContadoRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoCuotasRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoListadoResponseDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle.PagarCuotaRequest;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle.PagoDetalleResponseDTO;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IPagoService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pagos", description = "Gestion de pagos al contado, pagos en cuotas y cuotas pendientes.")
@SecurityRequirement(name = OpenApiConfig.SESSION_AUTH_SCHEME)
public class PagoController {

    @Autowired
    private IPagoService pagoService;

    @PostMapping("/contado")
    @Operation(summary = "Registrar un pago al contado", description = "Crea un comprobante de pago completo para un cliente y su paquete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago al contado registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida o regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class))),
            @ApiResponse(responseCode = "401", description = "Sesion no autenticada"),
            @ApiResponse(responseCode = "404", description = "Cliente o paquete no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<PagoListadoResponseDTO> createContado(@RequestBody @Valid PagoContadoRequestDTO pagoRequestDTO,
                                                                UriComponentsBuilder uriComponentsBuilder) {
        PagoListadoResponseDTO creado = pagoService.crearPagoContado(pagoRequestDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/pagos/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @PostMapping("/cuotas")
    @Operation(summary = "Registrar un pago fraccionado", description = "Crea el pago inicial y genera el detalle de cuotas pendientes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago en cuotas registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida o regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class))),
            @ApiResponse(responseCode = "401", description = "Sesion no autenticada"),
            @ApiResponse(responseCode = "404", description = "Cliente o paquete no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<PagoDetalleResponseDTO> createCuotas(@RequestBody @Valid PagoCuotasRequestDTO pagoRequestDTO,
                                                               UriComponentsBuilder uriComponentsBuilder) {
        PagoDetalleResponseDTO creado = pagoService.crearPagoCuotas(pagoRequestDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/pagos/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar pagos", description = "Devuelve el listado resumido de pagos registrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "Sesion no autenticada")
    })
    public List<PagoListadoResponseDTO> listar() {
        return pagoService.listarPagos();
    }

    @GetMapping("/detalle/{id}")
    @Operation(summary = "Obtener detalle de un pago", description = "Devuelve un pago con su informacion detallada y, si aplica, sus cuotas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "Sesion no autenticada"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public PagoDetalleResponseDTO obtenerDetalle(@Parameter(description = "Identificador del pago.", example = "25")
                                                 @PathVariable Long id) {
        return pagoService.obtenerPagoConCuotas(id);
    }

    @PatchMapping("/suspender/{id}")
    @Operation(summary = "Suspender un pago", description = "Cambia el estado de un pago activo para impedir nuevas operaciones sobre el.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pago suspendido correctamente"),
            @ApiResponse(responseCode = "401", description = "Sesion no autenticada"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<Void> suspenderPago(@Parameter(description = "Identificador del pago a suspender.", example = "25")
                                              @PathVariable Long id) {
        pagoService.suspenderPago(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{pagoId}/cuotas/{cuotaId}/pagar")
    @Operation(summary = "Pagar una cuota pendiente", description = "Registra el pago de una cuota concreta dentro de un pago fraccionado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cuota pagada correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida o regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class))),
            @ApiResponse(responseCode = "401", description = "Sesion no autenticada"),
            @ApiResponse(responseCode = "404", description = "Pago o cuota no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<Void> cancelarCuota(
            @Parameter(description = "Identificador del pago principal.", example = "25")
            @PathVariable Long pagoId,
            @Parameter(description = "Numero o identificador de la cuota a cancelar.", example = "2")
            @PathVariable Integer cuotaId,
            @RequestBody PagarCuotaRequest request) {

        pagoService.pagarCuota(pagoId, cuotaId, request.metodoPago());
        return ResponseEntity.noContent().build();
    }
}
