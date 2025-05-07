package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.SpecialtyDTO;
import com.kikisito.salus.api.dto.request.AddSpecialtyRequest;
import com.kikisito.salus.api.dto.response.SpecialtiesListResponse;
import com.kikisito.salus.api.entity.SpecialtyEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecialtyService {
    @Autowired
    private final SpecialtyRepository specialtyRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int MAX_ROWS_PER_PAGE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public SpecialtiesListResponse getAllSpecialties(Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        // Usamos los métodos Math.max y Math.min para asegurarnos de que
        // los valores de page y limit estén dentro de los límites permitidos
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        // Obtenemos las especialidades de la base de datos
        Page<SpecialtyEntity> specialties = specialtyRepository.findAll(PageRequest.of(page, limit));

        // Convertimos las especialidades a DTOs
        List<SpecialtyDTO> specialtyDTOS = specialties.stream()
                .map(e -> modelMapper.map(e, SpecialtyDTO.class))
                .toList();

        return SpecialtiesListResponse.builder()
                .count(specialties.getTotalElements())
                .specialties(specialtyDTOS)
                .build();
    }

    @Transactional(readOnly = true)
    public SpecialtiesListResponse searchSpecialties(String search, Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        List<SpecialtyEntity> specialties = specialtyRepository.findByNameContainingIgnoreCase(search, PageRequest.of(page, limit)).getContent();

        List<SpecialtyDTO> specialtyDTOS = specialties.stream()
                .map(e -> modelMapper.map(e, SpecialtyDTO.class))
                .toList();

        return SpecialtiesListResponse.builder()
                .count(specialties.size())
                .specialties(specialtyDTOS)
                .build();
    }

    @Transactional
    public SpecialtyDTO addSpecialty(AddSpecialtyRequest request) {
        SpecialtyEntity specialtyEntity = modelMapper.map(request, SpecialtyEntity.class);
        specialtyEntity = specialtyRepository.save(specialtyEntity);
        return modelMapper.map(specialtyEntity, SpecialtyDTO.class);
    }

    @Transactional(readOnly = true)
    public SpecialtyDTO getSpecialty(Integer id) {
        SpecialtyEntity specialtyEntity = specialtyRepository.findById(id).orElseThrow(DataNotFoundException::specialtyNotFound);
        return modelMapper.map(specialtyEntity, SpecialtyDTO.class);
    }

    @Transactional
    public SpecialtyDTO updateSpecialty(Integer id, AddSpecialtyRequest request) {
        SpecialtyEntity specialtyEntity = specialtyRepository.findById(id).orElseThrow(DataNotFoundException::specialtyNotFound);
        modelMapper.map(request, specialtyEntity);
        specialtyEntity = specialtyRepository.save(specialtyEntity);
        return modelMapper.map(specialtyEntity, SpecialtyDTO.class);
    }

    @Transactional
    public void deleteSpecialty(Integer id) {
        specialtyRepository.deleteById(id);
    }
}