package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.EspecialidadDTO;
import com.kikisito.salus.api.dto.PerfilMedicoDTO;
import com.kikisito.salus.api.dto.UsuarioDTO;
import com.kikisito.salus.api.dto.request.AddEspecialidadMedicoRequest;
import com.kikisito.salus.api.dto.request.AddPerfilMedicoToUserRequest;
import com.kikisito.salus.api.entity.EspecialidadEntity;
import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.EspecialidadRepository;
import com.kikisito.salus.api.repository.PerfilMedicoRepository;
import com.kikisito.salus.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerfilMedicoService {
    @Autowired
    private final PerfilMedicoRepository perfilMedicoRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final EspecialidadRepository especialidadRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public PerfilMedicoDTO addMedicoFromUser(AddPerfilMedicoToUserRequest addPerfilMedicoToUserRequest) {
        UserEntity userEntity = userRepository.findById(addPerfilMedicoToUserRequest.getUserId()).orElseThrow(DataNotFoundException::userNotFound);
        String numeroColegiado = addPerfilMedicoToUserRequest.getNumeroColegiado();

        // Comprobamos que el usuario no sea un médico ya y que el número de colegiado no exista
        if(perfilMedicoRepository.existsMedicoEntitiesByUser(userEntity)) {
            throw ConflictException.userAlreadyMedico();
        } else if (perfilMedicoRepository.existsMedicoEntitiesByNumeroColegiado(numeroColegiado)) {
            throw ConflictException.numeroColegiadoAlreadyExists();
        }

        // Creamos y guardamos el perfil médico
        PerfilMedicoEntity perfilMedicoEntity = PerfilMedicoEntity.builder()
                .user(userEntity)
                .numeroColegiado(numeroColegiado)
                .build();
        perfilMedicoEntity = perfilMedicoRepository.save(perfilMedicoEntity);

        // Devolvemos el perfil médico tras mapearlo a DTO con el usuario DTO
        PerfilMedicoDTO perfilMedicoDTO = modelMapper.map(perfilMedicoEntity, PerfilMedicoDTO.class);
        perfilMedicoDTO.setUser(modelMapper.map(userEntity, UsuarioDTO.class));
        return perfilMedicoDTO;
    }

    @Transactional(readOnly = true)
    public List<EspecialidadDTO> getEspecialidades(Integer medicoId) {
        PerfilMedicoEntity perfilMedicoEntity = perfilMedicoRepository.findById(medicoId).orElseThrow(DataNotFoundException::medicoNotFound);

        return perfilMedicoEntity.getEspecialidades().stream()
                .map(especialidad -> modelMapper.map(especialidad, EspecialidadDTO.class))
                .toList();
    }

    @Transactional
    public PerfilMedicoDTO addEspecialidadMedico(Integer medicoId, AddEspecialidadMedicoRequest addEspecialidadMedicoRequest) {
        PerfilMedicoEntity perfilMedicoEntity = perfilMedicoRepository.findById(medicoId).orElseThrow(DataNotFoundException::medicoNotFound);
        EspecialidadEntity especialidadEntity = especialidadRepository.findById(addEspecialidadMedicoRequest.getEspecialidadId()).orElseThrow(DataNotFoundException::especialidadNotFound);

        // Comprobamos que el médico no tenga ya la especialidad
        if(perfilMedicoEntity.getEspecialidades().contains(especialidadEntity)) {
            throw ConflictException.medicoAlreadyHasEspecialidad();
        }

        // Añadimos la especialidad al médico y guardamos
        perfilMedicoEntity.getEspecialidades().add(especialidadEntity);
        perfilMedicoRepository.save(perfilMedicoEntity);

        return modelMapper.map(perfilMedicoEntity, PerfilMedicoDTO.class);
    }

    @Transactional
    public PerfilMedicoDTO deleteEspecialidadMedico(Integer medicoId, Integer especialidadId) {
        PerfilMedicoEntity perfilMedicoEntity = perfilMedicoRepository.findById(medicoId).orElseThrow(DataNotFoundException::medicoNotFound);
        EspecialidadEntity especialidadEntity = especialidadRepository.findById(especialidadId).orElseThrow(DataNotFoundException::especialidadNotFound);

        perfilMedicoEntity.getEspecialidades().remove(especialidadEntity);
        perfilMedicoRepository.save(perfilMedicoEntity);

        return modelMapper.map(perfilMedicoEntity, PerfilMedicoDTO.class);
    }
}