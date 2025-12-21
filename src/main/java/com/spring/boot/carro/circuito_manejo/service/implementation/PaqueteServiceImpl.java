package com.spring.boot.carro.circuito_manejo.service.implementation;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Paquete;
import com.spring.boot.carro.circuito_manejo.persistence.repository.PaqueteRepository;
import com.spring.boot.carro.circuito_manejo.presentation.dto.paquete.PaqueteDTO;
import com.spring.boot.carro.circuito_manejo.service.exception.NotFoundException;
import com.spring.boot.carro.circuito_manejo.service.interfaces.PaqueteService;
import com.spring.boot.carro.circuito_manejo.util.mapper.PaqueteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaqueteServiceImpl implements PaqueteService {

    private final String NOT_FOUND_MSG = "Paquete no encontrado con el id: ";

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private PaqueteMapper paqueteMapper;

    @Transactional(readOnly = true)
    @Override
    public List<PaqueteDTO> listar() {
        return paqueteMapper.toResponseList(paqueteRepository.findByActivoTrue());
    }

    @Transactional(readOnly = true)
    @Override
    public PaqueteDTO obtenerPorId(Long id) {
        Paquete entity= paqueteRepository.findById(id)
                .orElseThrow(()-> new NotFoundException(NOT_FOUND_MSG+id));
        return paqueteMapper.toResponse(entity);
    }

    @Transactional
    @Override
    public PaqueteDTO crear(PaqueteDTO paqueteDTO) {
        Paquete entity = paqueteMapper.toEntity(paqueteDTO);
        return paqueteMapper.toResponse(paqueteRepository.save(entity));
    }

    @Transactional
    @Override
    public PaqueteDTO actualizar(Long id, PaqueteDTO paqueteDTO) {
        Paquete entity = paqueteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG+ id));
        paqueteMapper.updateEntityFromDto(paqueteDTO,entity);
        return paqueteMapper.toResponse(paqueteRepository.save(entity));
    }

    @Transactional
    @Override
    public void eliminar(Long id) {
        Paquete entity=paqueteRepository.findById(id)
                .orElseThrow(()->new NotFoundException(NOT_FOUND_MSG+id));
        entity.setActivo(false);
        paqueteRepository.save(entity);
    }

    @Override
    public boolean tienePagosAsociados(Long id) {
        return paqueteRepository.existsByPaqueteIdInPagos(id);
    }

}
