package com.spring.boot.carro.circuito_manejo.presentation.controller;

import com.spring.boot.carro.circuito_manejo.configuration.app.OpenApiConfig;
import com.spring.boot.carro.circuito_manejo.presentation.dto.ErrorDetailDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioResponseDTO;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IUsuarioService;
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
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Gestion de clientes del circuito de manejo.")
@SecurityRequirement(name = OpenApiConfig.SESSION_AUTH_SCHEME)
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene todos los clientes registrados.")
    public ResponseEntity<List<UsuarioResponseDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza la informacion de un cliente existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> update(@RequestBody @Valid UsuarioRequestDTO usuarioRequestDTO,
                                                     @Parameter(description = "Identificador del usuario.", example = "10")
                                                     @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuarioRequestDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por id", description = "Devuelve el detalle de un cliente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> findById(@Parameter(description = "Identificador del usuario.", example = "10")
                                                       @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un cliente del sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<Void> delete(@Parameter(description = "Identificador del usuario.", example = "10")
                                       @PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Registra un nuevo cliente en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida",
                    content = @Content(schema = @Schema(implementation = ErrorDetailDTO.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> create(@RequestBody @Valid UsuarioRequestDTO usuarioRequestDTO,
                                                     UriComponentsBuilder uriComponentsBuilder) {
        UsuarioResponseDTO creado = usuarioService.crear(usuarioRequestDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/usuarios/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }
}
