package com.spring.boot.carro.circuito_manejo.presentation.dto.pago;

import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoPagoEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PagoListadoResponseDTO {
    private Long id;
    private String numeroBoleta;
    private String nombreUsuario;
    private String apellidoUsuario;
    private String nombrePaquete;
    private BigDecimal monto;
    private TipoPagoEnum tipoPago;
    private EstadoPagoEnum estado;
    private LocalDateTime fechaPago;
}
