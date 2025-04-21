package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.AppointmentDTO;
import com.kikisito.salus.api.dto.request.AppointmentRequest;
import com.kikisito.salus.api.entity.AppointmentEntity;
import com.kikisito.salus.api.entity.AppointmentSlotEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    @Autowired
    private final AppointmentRepository appointmentRepository;

    @Autowired
    private MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAllDoctorAppointmentsByDate(Integer doctorId, LocalDate date) {
        MedicalProfileEntity medico = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);
        List<AppointmentEntity> citas = appointmentRepository.findBySlot_DoctorAndSlot_Date(medico, date);
        return citas.stream()
                .map(cita -> modelMapper.map(cita, AppointmentDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Integer appointmentId) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow(DataNotFoundException::appointmentNotFound);
        return modelMapper.map(appointment, AppointmentDTO.class);
    }


    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAllUserAppointments(Integer userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        List<AppointmentEntity> citas = appointmentRepository.findByPatient(user);

        return citas.stream()
                .map(cita -> modelMapper.map(cita, AppointmentDTO.class))
                .toList();
    }

    @Transactional
    public AppointmentDTO createAppointment(UserEntity patient, AppointmentRequest appointmentRequest) {
        // Comprobamos y obtenemos el slot de la cita que ha solicitado el usuario
        AppointmentSlotEntity appointmentSlot = appointmentSlotRepository.findById(appointmentRequest.getAppointmentSlot()).orElseThrow(DataNotFoundException::appointmentSlotNotFound);

        // Comprobamos que no tenga ninguna cita asociada, es decir, que esté disponible
        if(appointmentSlot.getAppointment() != null) {
            throw ConflictException.appointmentSlotIsAlreadyTaken();
        }

        // Creamos y guardamos la cita
        AppointmentEntity appointment = AppointmentEntity.builder()
                .slot(appointmentSlot)
                .patient(patient)
                .type(appointmentRequest.getType())
                .reason(appointmentRequest.getReason())
                .build();
        appointment = appointmentRepository.save(appointment);

        return modelMapper.map(appointment, AppointmentDTO.class);
    }
}