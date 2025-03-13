package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.EspecialidadDTO;
import com.kikisito.salus.api.dto.request.AddEspecialidadRequest;
import com.kikisito.salus.api.entity.EspecialidadEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.EspecialidadRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecialidadService {
    @Autowired
    private final EspecialidadRepository especialidadRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public EspecialidadDTO addEspecialidad(AddEspecialidadRequest request) {
        EspecialidadEntity especialidadEntity = modelMapper.map(request, EspecialidadEntity.class);
        especialidadEntity = especialidadRepository.save(especialidadEntity);
        return modelMapper.map(especialidadEntity, EspecialidadDTO.class);
    }

    @Transactional(readOnly = true)
    public List<EspecialidadDTO> getAllEspecialidades() {
        return especialidadRepository.findAll()
                .stream()
                .map(especialidad -> modelMapper.map(especialidad, EspecialidadDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public EspecialidadDTO getEspecialidad(Integer id) {
        EspecialidadEntity especialidadEntity = especialidadRepository.findById(id).orElseThrow(DataNotFoundException::especialidadNotFound);
        return modelMapper.map(especialidadEntity, EspecialidadDTO.class);
    }

    @Transactional
    public EspecialidadDTO updateEspecialidad(Integer id, AddEspecialidadRequest request) {
        EspecialidadEntity especialidadEntity = especialidadRepository.findById(id).orElseThrow(DataNotFoundException::especialidadNotFound);
        modelMapper.map(request, especialidadEntity);
        especialidadEntity = especialidadRepository.save(especialidadEntity);
        return modelMapper.map(especialidadEntity, EspecialidadDTO.class);
    }

    @Transactional
    public void deleteEspecialidad(Integer id) {
        especialidadRepository.deleteById(id);
    }
}