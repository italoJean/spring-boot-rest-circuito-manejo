package com.spring.boot.carro.circuito_manejo.presentation.controller;


import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoListadoResponseDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoContadoRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.PagoCuotasRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle.PagarCuotaRequest;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle.PagoDetalleResponseDTO;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IPagoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {

    @Autowired
    private IPagoService pagoService;

    @PostMapping("/contado")
    public ResponseEntity<PagoListadoResponseDTO> createContado(@RequestBody @Valid PagoContadoRequestDTO pagoRequestDTO, UriComponentsBuilder uriComponentsBuilder) {
        PagoListadoResponseDTO creado = pagoService.crearPagoContado(pagoRequestDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/pagos/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

         /* URI location= ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri(); */

        return ResponseEntity.created(location).body(creado);
    }

    @PostMapping("/cuotas")
    public ResponseEntity<PagoDetalleResponseDTO> createCuotas(@RequestBody @Valid PagoCuotasRequestDTO pagoRequestDTO, UriComponentsBuilder uriComponentsBuilder) {
        PagoDetalleResponseDTO creado = pagoService.crearPagoCuotas(pagoRequestDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/pagos/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

         /* URI location= ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri(); */

        return ResponseEntity.created(location).body(creado);
    }


    @GetMapping
    public List<PagoListadoResponseDTO> listar() {
        return pagoService.listarPagos();
    }

    @GetMapping("/detalle/{id}")
    public PagoDetalleResponseDTO obtenerDetalle(@PathVariable Long id) {
        return pagoService.obtenerPagoConCuotas(id);
    }

    @PatchMapping("/suspender/{id}")
    public void suspenderPago(@PathVariable Long id) {
        pagoService.suspenderPago(id);
        ResponseEntity.noContent().build();
    }

    @PutMapping("/{pagoId}/cuotas/{cuotaId}/pagar")
    public ResponseEntity<Void> cancelarCuota(@PathVariable Long pagoId, @PathVariable Integer cuotaId,
                                              @RequestBody PagarCuotaRequest request) {

        pagoService.pagarCuota(pagoId, cuotaId, request.metodoPago());
        return ResponseEntity.noContent().build();
    }
}