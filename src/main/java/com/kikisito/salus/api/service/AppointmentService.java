package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.AppointmentDTO;
import com.kikisito.salus.api.dto.ReducedAppointmentDTO;
import com.kikisito.salus.api.dto.request.AppointmentRequest;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import com.kikisito.salus.api.type.AppointmentStatusType;
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


    @Transactional(readOnly = true)
    public List<ReducedAppointmentDTO> getAllUserReducedAppointments(Integer userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        List<AppointmentEntity> citas = appointmentRepository.findByPatient(user);

        return citas.stream()
                .map(cita -> modelMapper.map(cita, ReducedAppointmentDTO.class))
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

    @Transactional
    public AppointmentDTO updateAppointmentDoctorObservations(Integer appointmentId, String doctorObservations) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow(DataNotFoundException::appointmentNotFound);
        appointment.setDoctorObservations(doctorObservations);
        appointment = appointmentRepository.save(appointment);
        return modelMapper.map(appointment, AppointmentDTO.class);
    }

    @Transactional
    public AppointmentDTO updateAppointmentStatus(Integer appointmentId, AppointmentStatusType status) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow(DataNotFoundException::appointmentNotFound);
        appointment.setStatus(status);
        appointment = appointmentRepository.save(appointment);
        return modelMapper.map(appointment, AppointmentDTO.class);
    }

    @Transactional(readOnly = true)
    public boolean canProfessionalAccessAppointment(Integer appointmentId, Integer doctorId) {
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow(DataNotFoundException::appointmentNotFound);

        // Comprobamos si el médico ha atendido a este paciente o la cita es de alguna de las especialidades que tiene
        boolean appointmentIsFromDoctor = appointment.getSlot().getDoctor().equals(doctor);

        List<SpecialtyEntity> specialties = doctor.getSpecialties();
        boolean appointmentIsFromDoctorSpecialty = specialties.stream()
                .anyMatch(specialty -> specialty.equals(appointment.getSlot().getSpecialty()));

        return appointmentIsFromDoctor || appointmentIsFromDoctorSpecialty;
    }

    @Transactional(readOnly = true)
    public boolean patientHasAtLeastOneAppointmentWithDoctor(Integer userId, Integer doctorId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        return appointmentRepository.existsBySlot_DoctorAndPatient(doctor, user);
    }
}