package com.spring.boot.carro.circuito_manejo.presentation.dto.pago;

import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.circuito_manejo.presentation.dto.paquete.PaqueteResumenDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioResumenDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PagoResumenDTO {

    private Long id;
    private PaqueteResumenDTO paquete;
    private UsuarioResumenDTO usuario;
    private String  numeroBoleta;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private EstadoPagoEnum estado;
}
