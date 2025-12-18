package com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle;

import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.MetodoPagoEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoPagoEnum;
import com.spring.boot.carro.circuito_manejo.presentation.dto.paquete.PaqueteResumenDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioResumenDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PagoDetalleResponseDTO {
    private Long id;
    private String numeroBoleta;

    private UsuarioResumenDTO usuario;
    private PaqueteResumenDTO paquete;

    private MetodoPagoEnum metodoPago;
    private BigDecimal monto;
    private TipoPagoEnum tipoPago;
    private EstadoPagoEnum estado;
    private LocalDateTime fechaPago;

    private List<CuotaResponseDTO> cuotas;
}
