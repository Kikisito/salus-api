package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.AgendaMedicoDTO;
import com.kikisito.salus.api.dto.request.AgendaMedicoRequest;
import com.kikisito.salus.api.entity.AgendaMedicoEntity;
import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.AusenciaMedicoRepository;
import com.kikisito.salus.api.repository.CitaRepository;
import com.kikisito.salus.api.repository.AgendaMedicoRepository;
import com.kikisito.salus.api.repository.PerfilMedicoRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendaService {
    @Autowired
    private final CitaRepository citaRepository;

    @Autowired
    private final AgendaMedicoRepository agendaMedicoRepository;

    @Autowired
    private final AusenciaMedicoRepository ausenciaMedicoRepository;

    @Autowired
    private final PerfilMedicoRepository perfilMedicoRepository;

    @Autowired
    private final ModelMapper modelMapper;

    @Transactional
    public AgendaMedicoDTO addAgendaMedico(AgendaMedicoRequest agendaMedicoRequest) {
        // Buscamos si existe el perfil del médico y lo recuperamos
        PerfilMedicoEntity medico = perfilMedicoRepository.findById(agendaMedicoRequest.getMedico()).orElseThrow(DataNotFoundException::medicoNotFound);

        // Obtenemos todas las agendas existentes para el médico y el día de la semana
        List<AgendaMedicoEntity> agendasExistentes = agendaMedicoRepository.findByMedico_IdAndDiaSemana(agendaMedicoRequest.getMedico(), agendaMedicoRequest.getDiaSemana());

        // Obtenemos los horarios de la nueva agenda
        LocalTime start1 = agendaMedicoRequest.getHoraInicio();
        LocalTime end1 = agendaMedicoRequest.getHoraFin();

        // Verificamos si hay colisión de horarios
        boolean horarioColapsa = agendasExistentes.stream().anyMatch(
                agenda -> {
                    LocalTime start2 = agenda.getHoraInicio();
                    LocalTime end2 = agenda.getHoraFin();
                    return timesOverlap(start1, end1, start2, end2);
                }
        );

        if(horarioColapsa) {
            throw ConflictException.horarioColapsa();
        }

        // Mapeamos la petición a la entidad y guardamos
        AgendaMedicoEntity agendaMedicoEntity = modelMapper.map(agendaMedicoRequest, AgendaMedicoEntity.class);
        agendaMedicoEntity.setMedico(medico);
        agendaMedicoEntity = agendaMedicoRepository.save(agendaMedicoEntity);

        // Mapeamos la entidad a DTO y retornamos
        return modelMapper.map(agendaMedicoEntity, AgendaMedicoDTO.class);
    }

    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Casos de colisión:
        // 1. start1 está dentro del rango [start2, end2)
        // 2. end1 está dentro del rango (start2, end2]
        // 3. inicio2 está dentro del rango [inicio1, fin1)
        // 4. fin2 está dentro del rango (inicio1, fin1]
        return  (start1.equals(start2) || start1.isAfter(start2) && start1.isBefore(end2)) ||
                (end1.isAfter(start2) && end1.isBefore(end2) || end1.equals(end2)) ||
                (start2.isAfter(start1) && start2.isBefore(end1)) ||
                (end2.isAfter(start1) && end2.isBefore(end1));
    }
}