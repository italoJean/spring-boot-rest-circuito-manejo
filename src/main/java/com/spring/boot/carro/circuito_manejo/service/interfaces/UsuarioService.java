package com.spring.boot.carro.circuito_manejo.service.interfaces;

import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {


    public List<UsuarioResponseDTO> listar();

    public UsuarioResponseDTO obtenerPorId(Long id);

    public UsuarioResponseDTO crear(UsuarioRequestDTO usuarioRequestDTO);

    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO usuarioRequestDTO);

    public void eliminar(Long id);

}
