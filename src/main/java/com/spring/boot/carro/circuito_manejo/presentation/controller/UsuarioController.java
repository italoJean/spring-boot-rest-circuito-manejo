package com.spring.boot.carro.circuito_manejo.presentation.controller;

import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioResponseDTO;
import com.spring.boot.carro.circuito_manejo.service.interfaces.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(@RequestBody @Valid UsuarioRequestDTO usuarioRequestDTO, @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuarioRequestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> create(@RequestBody @Valid UsuarioRequestDTO usuarioRequestDTO, UriComponentsBuilder uriComponentsBuilder) {
        UsuarioResponseDTO creado = usuarioService.crear(usuarioRequestDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/usuarios/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

         /* URI location= ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri(); */
        return ResponseEntity.created(location).body(creado);
    }
}