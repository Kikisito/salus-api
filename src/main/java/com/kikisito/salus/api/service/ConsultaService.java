package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.ConsultaDTO;
import com.kikisito.salus.api.dto.request.ConsultaRequest;
import com.kikisito.salus.api.dto.response.RoomsListResponse;
import com.kikisito.salus.api.entity.CentroMedicoEntity;
import com.kikisito.salus.api.entity.ConsultaEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.CentroMedicoRepository;
import com.kikisito.salus.api.repository.ConsultaRepository;
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
public class ConsultaService {
    @Autowired
    private final CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private final ConsultaRepository consultaRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int MAX_ROWS_PER_PAGE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public RoomsListResponse getConsultas(Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        // Usamos los métodos Math.max y Math.min para asegurarnos de que
        // los valores de page y limit estén dentro de los límites permitidos
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);
        
        Page<ConsultaEntity> consultas = consultaRepository.findAll(PageRequest.of(page, limit));

        // Convertimos los centros médicos a DTOs
        List<ConsultaDTO> consultaDTOs = consultas.getContent().stream()
                .map(c -> modelMapper.map(c, ConsultaDTO.class))
                .toList();

        return RoomsListResponse.builder()
                .count(consultas.getTotalElements())
                .rooms(consultaDTOs)
                .build();
    }

    @Transactional(readOnly = true)
    public RoomsListResponse searchConsultas(String search, Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        Page<ConsultaEntity> consultas = consultaRepository.findByNombreContainingIgnoreCaseOrCentroMedico_NombreContainingIgnoreCase(search, search, PageRequest.of(page, limit));

        // Convertimos los centros médicos a DTOs
        List<ConsultaDTO> consultaDTOs = consultas.getContent().stream()
                .map(c -> modelMapper.map(c, ConsultaDTO.class))
                .toList();

        return RoomsListResponse.builder()
                .count(consultas.getTotalElements())
                .rooms(consultaDTOs)
                .build();
    }

    @Transactional(readOnly = true)
    public ConsultaDTO getConsulta(Integer id) {
        ConsultaEntity consulta = consultaRepository.findById(id).orElseThrow(DataNotFoundException::consultaNotFound);
        return modelMapper.map(consulta, ConsultaDTO.class);
    }

    @Transactional
    public ConsultaDTO addConsulta(ConsultaRequest consultaRequest) {
        // Busqueda del centro medico
        CentroMedicoEntity centroMedico = centroMedicoRepository.findById(consultaRequest.getCentroMedico()).orElseThrow(DataNotFoundException::centroMedicoNotFound);

        // Mapeo de la entidad consulta y asignación del centro medico
        ConsultaEntity consulta = modelMapper.map(consultaRequest, ConsultaEntity.class);
        consulta.setCentroMedico(centroMedico);

        // Guardado en la base de datos
        consulta = consultaRepository.save(consulta);

        // Devolvemos el DTO de la consulta
        return modelMapper.map(consulta, ConsultaDTO.class);
    }

    @Transactional
    public ConsultaDTO updateConsulta(Integer id, ConsultaRequest consultaRequest) {
        CentroMedicoEntity centroMedico = centroMedicoRepository.findById(consultaRequest.getCentroMedico()).orElseThrow(DataNotFoundException::centroMedicoNotFound);
        ConsultaEntity consulta = consultaRepository.findById(id).orElseThrow(DataNotFoundException::consultaNotFound);

        modelMapper.map(consultaRequest, consulta);
        consulta.setCentroMedico(centroMedico);

        consulta = consultaRepository.save(consulta);

        return modelMapper.map(consulta, ConsultaDTO.class);
    }

    @Transactional
    public void deleteConsulta(Integer id) {
        consultaRepository.deleteById(id);
    }
}