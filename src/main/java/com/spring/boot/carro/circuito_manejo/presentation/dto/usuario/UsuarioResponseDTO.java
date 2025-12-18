package com.spring.boot.carro.circuito_manejo.presentation.dto.usuario;

import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoDocumentoEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UsuarioResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private TipoDocumentoEnum tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String email;
    private LocalDateTime fechaRegistro;
}
