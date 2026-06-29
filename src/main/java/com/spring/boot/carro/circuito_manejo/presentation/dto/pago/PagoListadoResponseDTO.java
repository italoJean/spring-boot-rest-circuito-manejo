package com.spring.boot.carro.circuito_manejo.presentation.dto.pago;

import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoPagoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Schema(description = "Resumen de un pago registrado en el sistema.")
public class PagoListadoResponseDTO {

    @Schema(description = "Identificador del pago.", example = "25")
    private Long id;

    @Schema(description = "Numero de comprobante o boleta generado.", example = "BOL-00025")
    private String numeroBoleta;

    @Schema(description = "Nombre del cliente.", example = "Italo")
    private String nombreUsuario;

    @Schema(description = "Apellido del cliente.", example = "Carlos")
    private String apellidoUsuario;

    @Schema(description = "Nombre del paquete comprado.", example = "Paquete Basico")
    private String nombrePaquete;

    @Schema(description = "Monto del pago registrado.", example = "350.00")
    private BigDecimal monto;

    @Schema(description = "Tipo de pago aplicado.", example = "CONTADO")
    private TipoPagoEnum tipoPago;

    @Schema(description = "Estado actual del pago.", example = "PAGADO")
    private EstadoPagoEnum estado;

    @Schema(description = "Fecha y hora del registro del pago.", example = "2026-05-25T14:00:00")
    private LocalDateTime fechaPago;
}
