package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.ConsultaDTO;
import com.kikisito.salus.api.dto.request.ConsultaRequest;
import com.kikisito.salus.api.entity.CentroMedicoEntity;
import com.kikisito.salus.api.entity.ConsultaEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.CentroMedicoRepository;
import com.kikisito.salus.api.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaService {
    @Autowired
    private final CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private final ConsultaRepository consultaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<ConsultaDTO> getConsultas() {
        List<ConsultaEntity> consultas = consultaRepository.findAll();

        return consultas.stream()
                .map(consulta -> modelMapper.map(consulta, ConsultaDTO.class))
                .toList();
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