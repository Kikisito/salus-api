package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.EspecialidadDTO;
import com.kikisito.salus.api.dto.request.AddEspecialidadRequest;
import com.kikisito.salus.api.dto.response.SpecialtiesListResponse;
import com.kikisito.salus.api.entity.EspecialidadEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.EspecialidadRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EspecialidadService {
    @Autowired
    private final EspecialidadRepository especialidadRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int MAX_ROWS_PER_PAGE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public SpecialtiesListResponse getAllEspecialidades(Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        // Usamos los métodos Math.max y Math.min para asegurarnos de que
        // los valores de page y limit estén dentro de los límites permitidos
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        // Obtenemos las especialidades de la base de datos
        List<EspecialidadEntity> especialidades = especialidadRepository.findAll(PageRequest.of(page, limit)).getContent();

        // Convertimos las especialidades a DTOs
        List<EspecialidadDTO> especialidadDTOs = especialidades.stream()
                .map(e -> modelMapper.map(e, EspecialidadDTO.class))
                .toList();

        return SpecialtiesListResponse.builder()
                .count(especialidades.size())
                .specialties(especialidadDTOs)
                .build();
    }

    @Transactional(readOnly = true)
    public SpecialtiesListResponse searchEspecialidades(String search, Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        List<EspecialidadEntity> especialidades = especialidadRepository.findByNombreContainingIgnoreCase(search, PageRequest.of(page, limit)).getContent();

        List<EspecialidadDTO> especialidadDTOs = especialidades.stream()
                .map(e -> modelMapper.map(e, EspecialidadDTO.class))
                .toList();

        return SpecialtiesListResponse.builder()
                .count(especialidades.size())
                .specialties(especialidadDTOs)
                .build();
    }

    @Transactional
    public EspecialidadDTO addEspecialidad(AddEspecialidadRequest request) {
        EspecialidadEntity especialidadEntity = modelMapper.map(request, EspecialidadEntity.class);
        especialidadEntity = especialidadRepository.save(especialidadEntity);
        return modelMapper.map(especialidadEntity, EspecialidadDTO.class);
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