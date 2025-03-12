package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.CentroMedicoDTO;
import com.kikisito.salus.api.dto.request.NewCentroMedicoRequest;
import com.kikisito.salus.api.entity.CentroMedicoEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.CentroMedicoRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CentroMedicoService {
    @Autowired
    private final CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<CentroMedicoDTO> getCentrosMedicos() {
        return centroMedicoRepository.findAll().stream()
                .map(centroMedicoEntity -> modelMapper.map(centroMedicoEntity, CentroMedicoDTO.class))
                .toList();
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