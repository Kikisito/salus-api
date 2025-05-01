package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.MedicalCenterDTO;
import com.kikisito.salus.api.dto.request.NewMedicalCenterRequest;
import com.kikisito.salus.api.dto.response.MedicalCentersListResponse;
import com.kikisito.salus.api.entity.MedicalCenterEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.MedicalCenterRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalCenterService {
    @Autowired
    private final MedicalCenterRepository medicalCenterRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int MAX_ROWS_PER_PAGE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public MedicalCentersListResponse getMedicalCenters(Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        // Usamos los métodos Math.max y Math.min para asegurarnos de que
        // los valores de page y limit estén dentro de los límites permitidos
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        // Obtenemos los centros médicos de la base de datos
        Page<MedicalCenterEntity> medicalCenters = medicalCenterRepository.findAll(PageRequest.of(page, limit));

        // Convertimos los centros médicos a DTOs
        List<MedicalCenterDTO> medicalCenterDTOS = medicalCenters.getContent().stream()
                .map(c -> modelMapper.map(c, MedicalCenterDTO.class))
                .toList();

        return MedicalCentersListResponse.builder()
                .count(medicalCenters.getTotalElements())
                .medicalCenters(medicalCenterDTOS)
                .build();
    }

    @Transactional(readOnly = true)
    public MedicalCentersListResponse searchMedicalCenters(String search, Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        // Usamos los métodos Math.max y Math.min para asegurarnos de que
        // los valores de page y limit estén dentro de los límites permitidos
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        // Obtenemos los centros médicos de la base de datos
        Page<MedicalCenterEntity> medicalCenters = medicalCenterRepository.findByNameContainingIgnoreCase(search, PageRequest.of(page, limit));

        // Convertimos los centros médicos a DTOs
        List<MedicalCenterDTO> medicalCenterDTOS = medicalCenters.getContent().stream()
                .map(c -> modelMapper.map(c, MedicalCenterDTO.class))
                .collect(Collectors.toList());

        return MedicalCentersListResponse.builder()
                .count(medicalCenters.getTotalElements())
                .medicalCenters(medicalCenterDTOS)
                .build();
    }

    @Transactional(readOnly = true)
    public MedicalCentersListResponse getMedicalCentersByAvailableSpecialtyAfterDate(Integer specialtyId, LocalDate date, Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        // Evitamos fechas pasadas
        if (date.isBefore(LocalDate.now())) {
            throw ConflictException.dateInPast();
        }
        
        // Usamos los métodos Math.max y Math.min para asegurarnos de que
        // los valores de page y limit estén dentro de los límites permitidos
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        // Obtenemos los centros médicos de la base de datos
        Page<MedicalCenterEntity> medicalCenters = medicalCenterRepository.findByAvailableSpecialtyAfterDate(specialtyId, date, PageRequest.of(page, limit));

        // Convertimos los centros médicos a DTOs
        List<MedicalCenterDTO> medicalCenterDTOS = medicalCenters.getContent().stream()
                .map(c -> modelMapper.map(c, MedicalCenterDTO.class))
                .collect(Collectors.toList());

        return MedicalCentersListResponse.builder()
                .count(medicalCenters.getTotalElements())
                .medicalCenters(medicalCenterDTOS)
                .build();
    }

    @Transactional(readOnly = true)
    public MedicalCentersListResponse searchMedicalCentersByAvailableSpecialtyAfterDate(Integer specialtyId, String search, LocalDate date, Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        // Evitamos fechas pasadas
        if (date.isBefore(LocalDate.now())) {
            throw ConflictException.dateInPast();
        }

        // Usamos los métodos Math.max y Math.min para asegurarnos de que
        // los valores de page y limit estén dentro de los límites permitidos
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        // Obtenemos los centros médicos de la base de datos
        Page<MedicalCenterEntity> medicalCenters = medicalCenterRepository.searchByAvailableSpecialtyAfterDate(specialtyId, search, date, PageRequest.of(page, limit));

        // Convertimos los centros médicos a DTOs
        List<MedicalCenterDTO> medicalCenterDTOS = medicalCenters.getContent().stream()
                .map(c -> modelMapper.map(c, MedicalCenterDTO.class))
                .collect(Collectors.toList());

        return MedicalCentersListResponse.builder()
                .count(medicalCenters.getTotalElements())
                .medicalCenters(medicalCenterDTOS)
                .build();
    }

    @Transactional(readOnly = true)
    public MedicalCenterDTO getMedicalCenterById(Integer id) {
        MedicalCenterEntity medicalCenter = medicalCenterRepository.findById(id).orElseThrow(DataNotFoundException::medicalCenterNotFound);
        return modelMapper.map(medicalCenter, MedicalCenterDTO.class);
    }

    @Transactional
    public MedicalCenterDTO addMedicalCenter(NewMedicalCenterRequest centroMedicoDTO) {
        MedicalCenterEntity medicalCenter = modelMapper.map(centroMedicoDTO, MedicalCenterEntity.class);
        medicalCenter = medicalCenterRepository.save(medicalCenter);
        return modelMapper.map(medicalCenter, MedicalCenterDTO.class);
    }

    @Transactional
    public MedicalCenterDTO updateMedicaslCenter(Integer id, NewMedicalCenterRequest centroMedicoDTO) {
        MedicalCenterEntity medicalCenter = medicalCenterRepository.findById(id).orElseThrow(DataNotFoundException::medicalCenterNotFound);
        modelMapper.map(centroMedicoDTO, medicalCenter);
        medicalCenter = medicalCenterRepository.save(medicalCenter);
        return modelMapper.map(medicalCenter, MedicalCenterDTO.class);
    }

    @Transactional
    public void deleteMedicalCenter(Integer id) {
        medicalCenterRepository.deleteById(id);
    }
}