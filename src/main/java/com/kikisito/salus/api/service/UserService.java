package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.DireccionDTO;
import com.kikisito.salus.api.dto.UsuarioDTO;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UsuarioDTO getCurrentProfile() {
        UserEntity userEntity = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DireccionDTO direccionDTO = null;
        if (userEntity.getDireccion() != null) {
            direccionDTO = DireccionDTO.builder()
                    .lineaDireccion1(userEntity.getDireccion().getLineaDireccion1())
                    .lineaDireccion2(userEntity.getDireccion().getLineaDireccion2())
                    .codigoPostal(userEntity.getDireccion().getCodigoPostal())
                    .municipio(userEntity.getDireccion().getMunicipio())
                    .localidad(userEntity.getDireccion().getLocalidad())
                    .provincia(userEntity.getDireccion().getProvincia())
                    .pais(userEntity.getDireccion().getPais())
                    .build();
        }

        return UsuarioDTO.builder()
                .nombre(userEntity.getNombre())
                .apellidos(userEntity.getApellidos())
                .nif(userEntity.getNif())
                .email(userEntity.getEmail())
                .telefono(userEntity.getTelefono())
                .fechaNacimiento(userEntity.getFechaNacimiento())
                .direccion(direccionDTO)
                .build();
    }
}