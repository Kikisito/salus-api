package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.PerfilMedicoDTO;
import com.kikisito.salus.api.dto.request.AddPerfilMedicoToUserRequest;
import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.PerfilMedicoRepository;
import com.kikisito.salus.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerfilMedicoService {
    @Autowired
    private final PerfilMedicoRepository perfilMedicoRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public PerfilMedicoDTO addMedicoFromUser(AddPerfilMedicoToUserRequest addPerfilMedicoToUserRequest) {
        UserEntity userEntity = userRepository.findById(addPerfilMedicoToUserRequest.getUserId()).orElseThrow(DataNotFoundException::userNotFound);
        String numeroColegiado = addPerfilMedicoToUserRequest.getNumeroColegiado();

        if(perfilMedicoRepository.existsMedicoEntitiesByUser(userEntity)) {
            throw ConflictException.userAlreadyMedico();
        } else if (perfilMedicoRepository.existsMedicoEntitiesByNumeroColegiado(numeroColegiado)) {
            throw ConflictException.numeroColegiadoAlreadyExists();
        }

        PerfilMedicoEntity perfilMedicoEntity = PerfilMedicoEntity.builder()
                .user(userEntity)
                .numeroColegiado(numeroColegiado)
                .build();
        perfilMedicoEntity = perfilMedicoRepository.save(perfilMedicoEntity);

        return modelMapper.map(perfilMedicoEntity, PerfilMedicoDTO.class);
    }
}