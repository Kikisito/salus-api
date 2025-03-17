package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.CitaSlotDTO;
import com.kikisito.salus.api.dto.request.CitaSlotRequest;
import com.kikisito.salus.api.entity.CitaSlotEntity;
import com.kikisito.salus.api.entity.ConsultaEntity;
import com.kikisito.salus.api.entity.EspecialidadEntity;
import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaSlotService {
    @Autowired
    private final CitaSlotRepository citaSlotRepository;

    @Autowired
    private final AgendaMedicoRepository agendaMedicoRepository;

    @Autowired
    private final AusenciaMedicoRepository ausenciaMedicoRepository;

    @Autowired
    private final PerfilMedicoRepository perfilMedicoRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Scheduled(cron = "0 0 6 * * *")
    public void appointmentSlotsAutomatedGeneration() {
        /*List<AgendaMedicoEntity> agenda = agendaMedicoRepository.findAll();

        for (AgendaMedicoEntity agendaMedicoEntity : agenda) {
            LocalTime inicio = agendaMedicoEntity.getHoraInicio();
            LocalTime fin = agendaMedicoEntity.getHoraFin();
            Integer duracion = agendaMedicoEntity.getDuracionCita();

            List<String> citas = new ArrayList<>();
            while (inicio.isBefore(fin)) {
                citas.add(inicio.toString());
                inicio = inicio.plusMinutes(duracion);
            }

            for (String cita : citas) {
                System.out.println(cita);
            }
        }*/
    }

    @Transactional(readOnly = true)
    public List<CitaSlotDTO> getCitasSlotByMedicoAndFecha(Integer medicoId, LocalDate fecha) {
        PerfilMedicoEntity medico = perfilMedicoRepository.findById(medicoId).orElseThrow(DataNotFoundException::medicoNotFound);
        return citaSlotRepository.findByPerfilMedicoAndFecha(medico, fecha);
    }

    @Transactional
    public CitaSlotDTO createCitaSlot(CitaSlotRequest citaSlotRequest) {
        // Recuperamos el medico, la especialidad y la consulta de la base de datos
        PerfilMedicoEntity medico = perfilMedicoRepository.findById(citaSlotRequest.getPerfilMedico()).orElseThrow(DataNotFoundException::medicoNotFound);
        EspecialidadEntity especialidad = especialidadRepository.findById(citaSlotRequest.getEspecialidad()).orElseThrow(DataNotFoundException::especialidadNotFound);
        ConsultaEntity consulta = consultaRepository.findById(citaSlotRequest.getConsulta()).orElseThrow(DataNotFoundException::consultaNotFound);

        // Creamos la CitaSlot
        CitaSlotEntity citaSlotEntity = CitaSlotEntity.builder()
                .perfilMedico(medico)
                .especialidad(especialidad)
                .consulta(consulta)
                .fecha(citaSlotRequest.getFecha())
                .hora(citaSlotRequest.getHora())
                .build();

        // Guardamos la CitaSlot en la base de datos
        citaSlotEntity = citaSlotRepository.save(citaSlotEntity);

        // Devolvemos la CitaSlot
        return modelMapper.map(citaSlotEntity, CitaSlotDTO.class);
    }

    @Transactional
    public void deleteCitaSlot(Integer id) {
        if(!citaSlotRepository.existsById(id)) {
            throw DataNotFoundException.citaSlotNotFound();
        }

        citaSlotRepository.deleteById(id);
    }
}