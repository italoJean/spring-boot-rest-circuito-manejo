package com.spring.boot.carro.circuito_manejo.persistence.entity;

import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoReservaEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "reservas")
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reserva_pago"))
    private Pago pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reserva_vehiculo"))
    private Vehiculo vehiculo;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(nullable = false)
    private Integer minutosReservados;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private EstadoReservaEnum estado;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Builder.Default
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventoReserva> eventos = new ArrayList<>();

    @Column(nullable = false)
    private boolean activo;

}
