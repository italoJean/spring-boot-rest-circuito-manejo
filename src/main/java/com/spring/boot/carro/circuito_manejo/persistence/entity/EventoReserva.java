package com.spring.boot.carro.circuito_manejo.persistence.entity;

import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoEventoReservaEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_reservas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoReserva {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pago", foreignKey = @ForeignKey(name = "fk_evento_pago"))
    private Pago pago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", foreignKey = @ForeignKey(name = "fk_evento_reserva"))
    private Reserva reserva;

    @Column(name = "minutos_reservados_antes")
    private Integer minutosReservadosAntes;

    @Column(name = "minutos_usados")
    private Integer minutosUsados;

    @Column(name = "minutos_devueltos")
    private Integer minutosDevueltos;

    @Column(name = "minutos_afectados", nullable = false)
    private Integer minutosAfectados;

    @Column(length = 255)
    private String detalle;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", length = 30, nullable = false)
    private TipoEventoReservaEnum tipo;

    @Column(name = "numero_reprogramacion")
    private Integer numeroReprogramacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}