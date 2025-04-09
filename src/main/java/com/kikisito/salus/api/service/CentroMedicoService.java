package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.CentroMedicoDTO;
import com.kikisito.salus.api.dto.request.NewCentroMedicoRequest;
import com.kikisito.salus.api.dto.response.MedicalCentersListResponse;
import com.kikisito.salus.api.entity.CentroMedicoEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.CentroMedicoRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CentroMedicoService {
    @Autowired
    private final CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int MAX_ROWS_PER_PAGE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public MedicalCentersListResponse getCentrosMedicos(Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        // Usamos los métodos Math.max y Math.min para asegurarnos de que
        // los valores de page y limit estén dentro de los límites permitidos
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        // Obtenemos los centros médicos de la base de datos
        Page<CentroMedicoEntity> centrosMedicos = centroMedicoRepository.findAll(PageRequest.of(page, limit));

        // Convertimos los centros médicos a DTOs
        List<CentroMedicoDTO> centroMedicoDTOs = centrosMedicos.getContent().stream()
                .map(c -> modelMapper.map(c, CentroMedicoDTO.class))
                .toList();

        return MedicalCentersListResponse.builder()
                .count(centrosMedicos.getTotalElements())
                .medicalCenters(centroMedicoDTOs)
                .build();
    }

    @Transactional(readOnly = true)
    public MedicalCentersListResponse searchCentrosMedicos(String search, Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        // Usamos los métodos Math.max y Math.min para asegurarnos de que
        // los valores de page y limit estén dentro de los límites permitidos
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        // Obtenemos los centros médicos de la base de datos
        Page<CentroMedicoEntity> centrosMedicos = centroMedicoRepository.findByNombreContainingIgnoreCase(search, PageRequest.of(page, limit));

        // Convertimos los centros médicos a DTOs
        List<CentroMedicoDTO> centroMedicoDTOs = centrosMedicos.getContent().stream()
                .map(c -> modelMapper.map(c, CentroMedicoDTO.class))
                .collect(Collectors.toList());

        return MedicalCentersListResponse.builder()
                .count(centrosMedicos.getTotalElements())
                .medicalCenters(centroMedicoDTOs)
                .build();
    }

    @Transactional(readOnly = true)
    public CentroMedicoDTO getCentroMedicoById(Integer id) {
        CentroMedicoEntity centroMedico = centroMedicoRepository.findById(id).orElseThrow(DataNotFoundException::centroMedicoNotFound);
        return modelMapper.map(centroMedico, CentroMedicoDTO.class);
    }

    @Transactional
    public CentroMedicoDTO addCentroMedico(NewCentroMedicoRequest centroMedicoDTO) {
        CentroMedicoEntity centroMedico = modelMapper.map(centroMedicoDTO, CentroMedicoEntity.class);
        centroMedico = centroMedicoRepository.save(centroMedico);
        return modelMapper.map(centroMedico, CentroMedicoDTO.class);
    }

    @Transactional
    public CentroMedicoDTO updateCentroMedico(Integer id, NewCentroMedicoRequest centroMedicoDTO) {
        CentroMedicoEntity centroMedico = centroMedicoRepository.findById(id).orElseThrow(DataNotFoundException::centroMedicoNotFound);
        modelMapper.map(centroMedicoDTO, centroMedico);
        centroMedico = centroMedicoRepository.save(centroMedico);
        return modelMapper.map(centroMedico, CentroMedicoDTO.class);
    }

    @Transactional
    public void deleteCentroMedico(Integer id) {
        centroMedicoRepository.deleteById(id);
    }
}