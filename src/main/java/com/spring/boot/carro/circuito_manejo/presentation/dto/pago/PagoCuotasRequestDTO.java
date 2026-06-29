package com.spring.boot.carro.circuito_manejo.presentation.dto.pago;

import com.spring.boot.carro.circuito_manejo.persistence.enums.MetodoPagoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Solicitud para registrar un pago inicial y generar cuotas pendientes para un paquete.")
public record PagoCuotasRequestDTO(
        @Schema(description = "Identificador del paquete contratado.", example = "1")
        @NotNull(message = "El id del paquete es obligatorio") Long paqueteId,
        @Schema(description = "Identificador del cliente que realiza el pago.", example = "10")
        @NotNull(message = "El id del usuario es obligatorio") Long usuarioId,
        @Schema(description = "Metodo usado para registrar el pago inicial.", example = "TRANSFERENCIA")
        @NotNull(message = "El metodo de pago es obligatorio") MetodoPagoEnum metodoPago,
        @Schema(description = "Cantidad total de cuotas pactadas.", example = "3")
        @NotNull
        @Positive
        Integer cuotas,
        @Schema(description = "Monto cancelado en el primer pago.", example = "150.00")
        @NotNull
        @Positive
        BigDecimal montoPrimerPago
) {}
