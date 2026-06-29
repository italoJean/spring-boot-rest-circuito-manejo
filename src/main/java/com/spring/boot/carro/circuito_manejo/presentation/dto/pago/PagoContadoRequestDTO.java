package com.spring.boot.carro.circuito_manejo.presentation.dto.pago;

import com.spring.boot.carro.circuito_manejo.persistence.enums.MetodoPagoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud para registrar un pago al contado asociado a un cliente y un paquete.")
public record PagoContadoRequestDTO(
        @Schema(description = "Identificador del paquete contratado.", example = "1")
        @NotNull(message = "El id del paquete es obligatorio") Long paqueteId,
        @Schema(description = "Identificador del cliente que realiza el pago.", example = "10")
        @NotNull(message = "El id del usuario es obligatorio") Long usuarioId,
        @Schema(description = "Metodo usado para registrar el pago.", example = "YAPE")
        @NotNull(message = "El metodo de pago es obligatorio") MetodoPagoEnum metodoPago
) {}
