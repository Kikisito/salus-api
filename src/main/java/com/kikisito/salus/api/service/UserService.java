package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.DireccionDTO;
import com.kikisito.salus.api.dto.UsuarioDTO;
import com.kikisito.salus.api.embeddable.DireccionEmbeddable;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public UsuarioDTO getCurrentProfile() {
        // Se obtiene el usuario autenticado
        UserEntity userEntity = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Se mapea la dirección a un DTO, si existe
        DireccionDTO direccionDTO = (userEntity.getDireccion() != null)
                ? modelMapper.map(userEntity.getDireccion(), DireccionDTO.class)
                : null;

        // Se mapea el usuario a un DTO y se le asigna la dirección para devolverlo como respuesta
        UsuarioDTO usuarioDTO = modelMapper.map(userEntity, UsuarioDTO.class);
        usuarioDTO.setDireccion(direccionDTO);
        return usuarioDTO;
    }

    @Transactional
    public UsuarioDTO updateAddress(DireccionDTO direccionDTO) {
        UserEntity userEntity = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return updateAddress(userEntity.getId(), direccionDTO);
    }

    @Transactional
    public UsuarioDTO updateAddress(int userId, DireccionDTO direccionDTO) {
        // Se busca el usuario por email
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);

        // Se mapea la dirección a un objeto "embeddable", se actualiza el usuario y se guarda
        DireccionEmbeddable direccionEmbeddable = modelMapper.map(direccionDTO, DireccionEmbeddable.class);
        userEntity.setDireccion(direccionEmbeddable);
        userEntity = userRepository.save(userEntity);

        // Se mapea el usuario actualizado a un DTO y se le asigna la dirección para devolverlo como respuesta
        UsuarioDTO usuarioDTOUpdated = modelMapper.map(userEntity, UsuarioDTO.class);
        usuarioDTOUpdated.setDireccion(direccionDTO);
        return usuarioDTOUpdated;
    }
}