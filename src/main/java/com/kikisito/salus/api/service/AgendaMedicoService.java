package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.AgendaMedicoDTO;
import com.kikisito.salus.api.dto.request.AgendaMedicoRequest;
import com.kikisito.salus.api.entity.AgendaMedicoEntity;
import com.kikisito.salus.api.entity.ConsultaEntity;
import com.kikisito.salus.api.entity.EspecialidadEntity;
import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.AgendaMedicoRepository;
import com.kikisito.salus.api.repository.ConsultaRepository;
import com.kikisito.salus.api.repository.EspecialidadRepository;
import com.kikisito.salus.api.repository.PerfilMedicoRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendaMedicoService {
    @Autowired
    private final AgendaMedicoRepository agendaMedicoRepository;

    @Autowired
    private final PerfilMedicoRepository perfilMedicoRepository;

    @Autowired
    private final EspecialidadRepository especialidadRepository;

    @Autowired
    private final ConsultaRepository consultaRepository;

    @Autowired
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<AgendaMedicoDTO> getAgendasMedico(Integer medicoId) {
        PerfilMedicoEntity medico = perfilMedicoRepository.findById(medicoId).orElseThrow(DataNotFoundException::medicoNotFound);
        List<AgendaMedicoEntity> agendas = agendaMedicoRepository.findByMedico(medico);
        return agendas.stream().map(agenda -> modelMapper.map(agenda, AgendaMedicoDTO.class)).toList();
    }

    @Transactional(readOnly = true)
    public List<AgendaMedicoDTO> getAgendasMedicoByDiaSemana(Integer medicoId, DayOfWeek diaSemana) {
        PerfilMedicoEntity medico = perfilMedicoRepository.findById(medicoId).orElseThrow(DataNotFoundException::medicoNotFound);
        List<AgendaMedicoEntity> agendas = agendaMedicoRepository.findByMedicoAndDiaSemana(medico, diaSemana);
        return agendas.stream().map(agenda -> modelMapper.map(agenda, AgendaMedicoDTO.class)).toList();
    }

    @Transactional
    public AgendaMedicoDTO addAgendaMedicoEntry(AgendaMedicoRequest agendaMedicoRequest) {
        // Buscamos si existe el perfil del médico y lo recuperamos
        PerfilMedicoEntity medico = perfilMedicoRepository.findById(agendaMedicoRequest.getMedico()).orElseThrow(DataNotFoundException::medicoNotFound);

        // Buscamos la especialidad y la consulta
        EspecialidadEntity especialidad = especialidadRepository.findById(agendaMedicoRequest.getEspecialidad()).orElseThrow(DataNotFoundException::especialidadNotFound);

        // Comprobamos si el médico tiene la especialidad de la petición
        if(!medico.getEspecialidades().contains(especialidad)) {
            throw ConflictException.doctorDoesNotHaveSpecialty();
        }

        // Buscamos la consulta
        ConsultaEntity consulta = consultaRepository.findById(agendaMedicoRequest.getConsulta()).orElseThrow(DataNotFoundException::consultaNotFound);

        // Obtenemos todas las agendas existentes para el médico y el día de la semana
        List<AgendaMedicoEntity> agendasExistentes = agendaMedicoRepository.findByMedicoAndDiaSemana(medico, agendaMedicoRequest.getDiaSemana());

        // Obtenemos los horarios de la nueva
        DayOfWeek diaSemana = agendaMedicoRequest.getDiaSemana();
        LocalTime start1 = agendaMedicoRequest.getHoraInicio();
        LocalTime end1 = agendaMedicoRequest.getHoraFin();

        // Verificamos si hay colisión de horarios
        boolean horarioColapsa = this.agendaOverlap(diaSemana, start1, end1, agendasExistentes);

        if (horarioColapsa) {
            throw ConflictException.horarioColapsa();
        }

        // Mapeamos la petición a la entidad y guardamos
        AgendaMedicoEntity agendaMedicoEntity = modelMapper.map(agendaMedicoRequest, AgendaMedicoEntity.class);
        agendaMedicoEntity.setMedico(medico);
        agendaMedicoEntity.setEspecialidad(especialidad);
        agendaMedicoEntity.setConsulta(consulta);
        agendaMedicoEntity = agendaMedicoRepository.save(agendaMedicoEntity);

        // Mapeamos la entidad a DTO y retornamos
        return modelMapper.map(agendaMedicoEntity, AgendaMedicoDTO.class);
    }

    @Transactional
    public AgendaMedicoDTO updateAgendaMedicoEntry(Integer agendaId, AgendaMedicoRequest agendaMedicoRequest) {
        // Buscamos la agenda a actualizar
        AgendaMedicoEntity agenda = agendaMedicoRepository.findById(agendaId).orElseThrow(DataNotFoundException::agendaNotFound);

        // Obtenemos todas las agendas existentes para el médico y el día de la semana
        List<AgendaMedicoEntity> agendasExistentes = agendaMedicoRepository.findByMedicoAndDiaSemana(agenda.getMedico(), agenda.getDiaSemana());

        // Obtenemos los horarios de la nueva
        DayOfWeek diaSemana = agendaMedicoRequest.getDiaSemana();
        LocalTime start1 = agendaMedicoRequest.getHoraInicio();
        LocalTime end1 = agendaMedicoRequest.getHoraFin();

        // Verificamos si hay colisión de horarios
        boolean horarioColapsa = this.agendaOverlap(diaSemana, start1, end1, agendasExistentes);

        if (horarioColapsa) {
            throw ConflictException.horarioColapsa();
        }

        // Mapeamos la petición a la entidad y guardamos
        agenda.setDiaSemana(agendaMedicoRequest.getDiaSemana());
        agenda.setHoraInicio(agendaMedicoRequest.getHoraInicio());
        agenda.setHoraFin(agendaMedicoRequest.getHoraFin());
        agenda = agendaMedicoRepository.save(agenda);

        // Mapeamos la entidad a DTO y retornamos
        return modelMapper.map(agenda, AgendaMedicoDTO.class);
    }

    @Transactional
    public void deleteAgendaMedicoEntry(Integer agendaId) {
        if (!agendaMedicoRepository.existsById(agendaId)) {
            throw DataNotFoundException.agendaNotFound();
        }
        agendaMedicoRepository.deleteById(agendaId);
    }

    private boolean agendaOverlap(DayOfWeek diaSemana, LocalTime start1, LocalTime end1, List<AgendaMedicoEntity> agendas) {
        return agendas.stream().anyMatch(
                agenda -> {
                    if (agenda.getDiaSemana() != diaSemana) {
                        return false;
                    }
                    LocalTime start2 = agenda.getHoraInicio();
                    LocalTime end2 = agenda.getHoraFin();
                    return timesOverlap(start1, end1, start2, end2);
                }
        );

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