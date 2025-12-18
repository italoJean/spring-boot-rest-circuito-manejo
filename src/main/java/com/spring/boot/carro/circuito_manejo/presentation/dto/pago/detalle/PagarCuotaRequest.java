package com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle;

import com.spring.boot.carro.circuito_manejo.persistence.enums.MetodoPagoEnum;

public record PagarCuotaRequest(
        MetodoPagoEnum metodoPago) {
}
