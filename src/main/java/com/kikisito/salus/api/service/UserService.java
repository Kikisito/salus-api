package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.DireccionDTO;
import com.kikisito.salus.api.dto.UsuarioDTO;
import com.kikisito.salus.api.dto.request.LoginRequest;
import com.kikisito.salus.api.dto.request.RegisterRequest;
import com.kikisito.salus.api.dto.response.AuthenticationResponse;
import com.kikisito.salus.api.entity.DireccionEntity;
import com.kikisito.salus.api.entity.PasswordResetEntity;
import com.kikisito.salus.api.entity.SessionEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.exception.InvalidTokenException;
import com.kikisito.salus.api.repository.DireccionRepository;
import com.kikisito.salus.api.repository.PasswordResetRepository;
import com.kikisito.salus.api.repository.SessionRepository;
import com.kikisito.salus.api.repository.UserRepository;
import com.kikisito.salus.api.type.AccountStatusType;
import com.kikisito.salus.api.type.RoleType;
import com.kikisito.salus.api.type.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

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