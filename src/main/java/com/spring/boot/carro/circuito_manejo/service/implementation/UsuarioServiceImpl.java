package com.spring.boot.carro.circuito_manejo.service.implementation;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Usuario;
import com.spring.boot.carro.circuito_manejo.persistence.repository.UsuarioRepository;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioResponseDTO;
import com.spring.boot.carro.circuito_manejo.service.exception.BusinessException;
import com.spring.boot.carro.circuito_manejo.service.exception.NotFoundException;
import com.spring.boot.carro.circuito_manejo.service.interfaces.UsuarioService;
import com.spring.boot.carro.circuito_manejo.util.mapper.UsuarioMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final String NOT_FOUND_MSG = "Usuario no encontrado con el id: ";

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioResponseDTO> listar() {
        return usuarioMapper.toResponseList(usuarioRepository.findByActivoTrue());
    }

    @Transactional(readOnly = true)
    @Override
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));
        return usuarioMapper.toResponse(entity);
    }

    @Transactional
    @Override
    public UsuarioResponseDTO crear(UsuarioRequestDTO usuarioRequestDTO) {

        usuarioRepository.findByNumeroDocumento(usuarioRequestDTO.getNumeroDocumento())
                .ifPresent(u -> {
                    throw new BusinessException("Ya existe un usuario con el número de documento: "
                            + usuarioRequestDTO.getNumeroDocumento());
                });

        usuarioRepository.findByEmail(usuarioRequestDTO.getEmail())
                .ifPresent(u -> {
                    throw new BusinessException("Ya existe un usuario con el correo: "
                            + usuarioRequestDTO.getEmail());
                });

        Usuario entity = usuarioMapper.toEntity(usuarioRequestDTO);
        entity.setFechaRegistro(LocalDateTime.now());

        return usuarioMapper.toResponse(usuarioRepository.save(entity));
    }

    @Transactional
    @Override
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO usuarioRequestDTO) {
        Usuario entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));

        usuarioRepository.findByNumeroDocumento(usuarioRequestDTO.getNumeroDocumento())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BusinessException("Ya existe otro usuario con el número de documento: "
                            + usuarioRequestDTO.getNumeroDocumento());
                });

        usuarioRepository.findByEmail(usuarioRequestDTO.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BusinessException("Ya existe otro usuario con el correo: "
                            + usuarioRequestDTO.getEmail());
                });

        usuarioMapper.updateEntityFromDto(usuarioRequestDTO, entity);
        return usuarioMapper.toResponse(usuarioRepository.save(entity));
    }

    @Transactional
    @Override
    public void eliminar(Long id) {
        Usuario entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));
        entity.setActivo(false);
        usuarioRepository.save(entity);
    }
}
