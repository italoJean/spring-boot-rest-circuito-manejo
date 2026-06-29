package com.spring.boot.carro.circuito_manejo.presentation.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "Estructura estandar de error devuelta por la API.")
public class ErrorDetailDTO {
    @Schema(description = "Mensaje principal del error.", example = "Error de validacion")
    private String mensaje;
    @Schema(description = "Codigo funcional o tecnico del error.", example = "VALIDATION_ERROR")
    private String codigo;
    @Schema(description = "Ruta del endpoint donde ocurrio el error.", example = "/api/v1/reservas")
    private String path;
    @Schema(description = "Fecha y hora en la que se genero el error.", example = "2026-05-25T15:30:00")
    private LocalDateTime timeStamp;
    @ArraySchema(schema = @Schema(description = "Detalle puntual de cada validacion o error detectado.", example = "fechaReserva: no debe ser nula"))
    private List<String> detalles;

    public ErrorDetailDTO(String mensaje, String codigo, String path, List<String> detalles) {
        this.mensaje = mensaje;
        this.codigo = codigo;
        this.path = path;
        this.detalles = detalles;
        this.timeStamp = LocalDateTime.now();
    }
}
