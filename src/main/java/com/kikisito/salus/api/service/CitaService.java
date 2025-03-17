package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.CitaDTO;
import com.kikisito.salus.api.dto.request.CitaRequest;
import com.kikisito.salus.api.entity.CitaEntity;
import com.kikisito.salus.api.entity.CitaSlotEntity;
import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {
    @Autowired
    private final CitaRepository citaRepository;

    @Autowired
    private PerfilMedicoRepository perfilMedicoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CitaSlotRepository citaSlotRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<CitaDTO> getAllDoctorAppointmentsByDate(Integer doctorId, LocalDate date) {
        PerfilMedicoEntity medico = perfilMedicoRepository.findById(doctorId).orElseThrow(DataNotFoundException::medicoNotFound);
        List<CitaEntity> citas = citaRepository.findBySlot_PerfilMedicoAndSlot_Fecha(medico, date);
        return citas.stream()
                .map(cita -> modelMapper.map(cita, CitaDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CitaDTO> getAllUserAppointments(Integer userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        List<CitaEntity> citas = citaRepository.findByPaciente(user);

        return citas.stream()
                .map(cita -> modelMapper.map(cita, CitaDTO.class))
                .toList();
    }

    @Transactional
    public CitaDTO obtainAppointment(UserEntity paciente, CitaRequest citaRequest) {
        // Comprobamos y obtenemos el slot de la cita que ha solicitado el usuario
        CitaSlotEntity citaSlot = citaSlotRepository.findById(citaRequest.getCitaSlot()).orElseThrow(DataNotFoundException::citaSlotNotFound);

        // Comprobamos que no tenga ninguna cita asociada, es decir, que esté disponible
        if(citaSlot.getCita() != null) {
            throw ConflictException.appointmentSlotIsAlreadyTaken();
        }

        // Creamos y guardamos la cita
        CitaEntity cita = CitaEntity.builder()
                .slot(citaSlot)
                .paciente(paciente)
                .tipo(citaRequest.getTipo())
                .motivo(citaRequest.getMotivo())
                .build();
        cita = citaRepository.save(cita);

        return modelMapper.map(cita, CitaDTO.class);
    }
}