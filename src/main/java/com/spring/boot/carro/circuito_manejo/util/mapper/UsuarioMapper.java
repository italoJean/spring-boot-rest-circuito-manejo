package com.spring.boot.carro.circuito_manejo.util.mapper;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Usuario;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioRequestDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioResponseDTO;
import com.spring.boot.carro.circuito_manejo.presentation.dto.usuario.UsuarioResumenDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "fechaRegistro", ignore = true)
//    @Mapping(target = "fechaRegistro", expression = "java(java.time.LocalDateTime.now())")
    Usuario toEntity(UsuarioRequestDTO usuarioRequestDTO);

    UsuarioResponseDTO toResponse(Usuario usuario);

    List<UsuarioResponseDTO> toResponseList(List<Usuario> usuarios);

    @Named("toUsuarioResumenDTO")
    UsuarioResumenDTO toResumenDTO(Usuario usuario);


    /**
     * Permite actualizar una entidad existente a partir de un DTO.
     * Ignora el 'id' para no intentar cambiar la clave primaria.
     *
     * @param usuarioRequestDTO Los nuevos datos.
     * @param entity            La entidad existente a modificar.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateEntityFromDto(UsuarioRequestDTO usuarioRequestDTO, @MappingTarget Usuario entity);
}
