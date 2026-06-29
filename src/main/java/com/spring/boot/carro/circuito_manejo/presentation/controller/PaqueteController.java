package com.spring.boot.carro.circuito_manejo.presentation.controller;

import com.spring.boot.carro.circuito_manejo.configuration.app.OpenApiConfig;
import com.spring.boot.carro.circuito_manejo.presentation.dto.ErrorDetailDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.paquete.PaqueteDTO;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IPaqueteService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/paquetes")
@Tag(name = "Paquetes", description = "Gestion de planes o paquetes de clases del circuito de manejo.")
@SecurityRequirement(name = OpenApiConfig.SESSION_AUTH_SCHEME)
public class PaqueteController {

    @Autowired
    private IPaqueteService paqueteService;

    @GetMapping
    @Operation(summary = "Listar paquetes", description = "Obtiene todos los paquetes disponibles en el sistema.")
    public ResponseEntity<List<PaqueteDTO>> findAll() {
        return ResponseEntity.ok(paqueteService.listar());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar paquete", description = "Actualiza la informacion de un paquete existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paquete actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paquete no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<PaqueteDTO> update(@RequestBody @Valid PaqueteDTO paqueteDTO,
                                             @Parameter(description = "Identificador del paquete.", example = "1")
                                             @PathVariable Long id) {
        return ResponseEntity.ok(paqueteService.actualizar(id, paqueteDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener paquete por id", description = "Devuelve el detalle de un paquete especifico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paquete encontrado"),
            @ApiResponse(responseCode = "404", description = "Paquete no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<PaqueteDTO> findById(@Parameter(description = "Identificador del paquete.", example = "1")
                                               @PathVariable Long id) {
        return ResponseEntity.ok(paqueteService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar paquete", description = "Elimina logicamente o fisicamente un paquete, segun la logica implementada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Paquete eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Paquete no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<Void> delete(@Parameter(description = "Identificador del paquete.", example = "1")
                                       @PathVariable Long id) {
        paqueteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @Operation(summary = "Crear paquete", description = "Registra un nuevo paquete de clases en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Paquete creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<PaqueteDTO> create(@RequestBody @Valid PaqueteDTO paqueteDTO,
                                             UriComponentsBuilder uriComponentsBuilder) {
        PaqueteDTO creado = paqueteService.crear(paqueteDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/paquetes/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @GetMapping("/{id}/tiene-pagos")
    @Operation(summary = "Verificar pagos asociados", description = "Indica si un paquete tiene pagos relacionados registrados.")
    public ResponseEntity<Boolean> verificarPagos(@Parameter(description = "Identificador del paquete.", example = "1")
                                                  @PathVariable Long id) {
        boolean tienePagos = paqueteService.tienePagosAsociados(id);
        return ResponseEntity.ok(tienePagos);
    }
}
