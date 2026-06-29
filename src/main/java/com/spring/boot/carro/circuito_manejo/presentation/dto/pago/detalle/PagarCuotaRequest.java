package com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle;

import com.spring.boot.carro.circuito_manejo.persistence.enums.MetodoPagoEnum;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud para cancelar una cuota pendiente de un pago fraccionado.")
public record PagarCuotaRequest(
        @Schema(description = "Metodo de pago usado para cancelar la cuota.", example = "EFECTIVO")
        MetodoPagoEnum metodoPago) {
}
