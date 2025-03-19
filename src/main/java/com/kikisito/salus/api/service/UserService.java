package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.DireccionDTO;
import com.kikisito.salus.api.dto.UsuarioDTO;
import com.kikisito.salus.api.dto.request.RestrictUserRequest;
import com.kikisito.salus.api.dto.response.UsersListResponse;
import com.kikisito.salus.api.embeddable.DireccionEmbeddable;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.UserRepository;
import com.kikisito.salus.api.type.AccountStatusType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public UsersListResponse getAllUsers(Integer page, Integer limit) {
        Page<UserEntity> userEntities = userRepository.findAll(PageRequest.of(page, limit));
        List<UsuarioDTO> usersDTO = userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UsuarioDTO.class))
                .toList();

        return UsersListResponse.builder()
                .count(userRepository.count())
                .users(usersDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public UsersListResponse searchUsers(String search, Integer page, Integer limit) {
        Page<UserEntity> userEntities = userRepository.searchUsers(search, PageRequest.of(page, limit));
        List<UsuarioDTO> usersDTO = userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UsuarioDTO.class))
                .toList();

        return UsersListResponse.builder()
                .count(userRepository.searchUsersCount(search))
                .users(usersDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO getUserProfile(Integer id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(DataNotFoundException::userNotFound);
        return this.getUserProfile(userEntity);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO getUserProfile(UserEntity userEntity) {
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
    public UsuarioDTO updateProfile(int userIdTarget, UsuarioDTO usuarioDTO) {
        // Se busca el usuario por ID
        UserEntity userEntity = userRepository.findById(userIdTarget).orElseThrow(DataNotFoundException::userNotFound);
        return this.updateProfile(userEntity, usuarioDTO);
    }

    @Transactional
    public UsuarioDTO updateProfile(UserEntity userEntity, UsuarioDTO usuarioDTO) {
        // Se comprueba si el email o el NIF ya están registrados por *OTRO* usuario
        UserEntity existingUser = userRepository.findByEmail(usuarioDTO.getEmail()).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userEntity.getId())) {
            throw ConflictException.emailIsRegistered();
        }

        existingUser = userRepository.findByNif(usuarioDTO.getNif()).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userEntity.getId())) {
            throw ConflictException.nifIsRegistered();
        }

        // Si el email, que estaba verificado, se ha cambiado, se vuelve a marcar como cuenta no verificada
        if (!userEntity.getEmail().equals(usuarioDTO.getEmail()) && userEntity.getAccountStatusType().equals(AccountStatusType.VERIFIED)) {
            userEntity.setAccountStatusType(AccountStatusType.NOT_VERIFIED);
        }

        // Se mapea el DTO a la entidad y se guarda
        userEntity.setNombre(usuarioDTO.getNombre());
        userEntity.setApellidos(usuarioDTO.getApellidos());
        userEntity.setNif(usuarioDTO.getNif());
        userEntity.setSexo(usuarioDTO.getSexo());
        userEntity.setEmail(usuarioDTO.getEmail());
        userEntity.setTelefono(usuarioDTO.getTelefono());
        userEntity.setFechaNacimiento(usuarioDTO.getFechaNacimiento());
        userEntity = userRepository.save(userEntity);

        // Se mapea el usuario actualizado a un DTO y se devuelve
        return modelMapper.map(userEntity, UsuarioDTO.class);
    }

    @Transactional
    public UsuarioDTO updateAddress(int userId, DireccionDTO direccionDTO) {
        // Se busca el usuario por ID
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        return updateAddress(userEntity, direccionDTO);
    }

    @Transactional
    public UsuarioDTO updateAddress(UserEntity userEntity, DireccionDTO direccionDTO) {
        // Se mapea la dirección a un objeto "embeddable", se actualiza el usuario y se guarda
        DireccionEmbeddable direccionEmbeddable = modelMapper.map(direccionDTO, DireccionEmbeddable.class);
        userEntity.setDireccion(direccionEmbeddable);
        userEntity = userRepository.save(userEntity);

        // Se mapea el usuario actualizado a un DTO y se le asigna la dirección para devolverlo como respuesta
        UsuarioDTO usuarioDTOUpdated = modelMapper.map(userEntity, UsuarioDTO.class);
        usuarioDTOUpdated.setDireccion(direccionDTO);
        return usuarioDTOUpdated;
    }

    @Transactional
    public UsuarioDTO restrictUser(int userId, RestrictUserRequest request) {
        // Se busca el usuario por ID
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);

        // Se restringe o se des-restringe el usuario
        userEntity.setRestricted(request.isRestrict());
        userEntity = userRepository.save(userEntity);

        return modelMapper.map(userEntity, UsuarioDTO.class);
    }
}