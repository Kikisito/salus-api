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
import java.time.LocalTime;
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

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private MedicalTestRepository medicalTestRepository;

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
    public List<AppointmentDTO> getUserAppointments(Integer userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        List<AppointmentEntity> citas = appointmentRepository.findByPatient(user);

        return citas.stream()
                .map(cita -> modelMapper.map(cita, AppointmentDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientAppointmentsWithDoctorOrItsSpecialties(Integer patientId, Integer doctorId) {
        UserEntity user = userRepository.findById(patientId).orElseThrow(DataNotFoundException::userNotFound);
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        List<AppointmentEntity> appointments = appointmentRepository.findByPatientWithDoctorOrItsSpecialties(user, doctor, doctor.getSpecialties());

        return appointments.stream()
                .map(appointment -> modelMapper.map(appointment, AppointmentDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReducedAppointmentDTO> getUserUpcomingAppointmentsReduced(Integer userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        List<AppointmentEntity> citas = appointmentRepository.findUpcomingAppointmentsByPatient(user);

        return citas.stream()
                .map(cita -> modelMapper.map(cita, ReducedAppointmentDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReducedAppointmentDTO> getUserPastAppointmentsReduced(Integer userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        List<AppointmentEntity> citas = appointmentRepository.findPastAppointmentsByPatient(user);

        return citas.stream()
                .map(cita -> modelMapper.map(cita, ReducedAppointmentDTO.class))
                .toList();
    }

    @Transactional
    public AppointmentDTO createAppointment(UserEntity patient, AppointmentRequest appointmentRequest) {
        // Comprobamos y obtenemos el slot de la cita que ha solicitado el usuario
        AppointmentSlotEntity appointmentSlot = appointmentSlotRepository.findById(appointmentRequest.getAppointmentSlot()).orElseThrow(DataNotFoundException::appointmentSlotNotFound);

        // Comprobamos que la cita no esté en el pasado
        if (
                appointmentSlot.getDate().isBefore(LocalDate.now())
                || (appointmentSlot.getDate().isEqual(LocalDate.now()) && appointmentSlot.getStartTime().isBefore(LocalTime.now()))
        ) {
            throw ConflictException.dateInPast();
        }

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

    @Transactional
    public void deleteAppointment(Integer appointmentId) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow(DataNotFoundException::appointmentNotFound);

        // Comprobamos que no falten menos de 24 horas para la cita
        if (appointment.getSlot().getDate().isBefore(LocalDate.now().plusDays(1))) {
            throw ConflictException.appointmentCannotBeDeleted();
        }

        // Desvinculamos de la cita todos los posibles ficheros asociados a ella
        if(appointment.getReports() != null && !appointment.getReports().isEmpty()) {
            appointment.getReports().forEach(report -> {
                report.setAppointment(null);
                reportRepository.save(report);
            });
        }

        if(appointment.getPrescriptions() != null && !appointment.getPrescriptions().isEmpty()) {
            appointment.getPrescriptions().forEach(prescription -> {
                prescription.setAppointment(null);
                prescriptionRepository.save(prescription);
            });
        }

        if(appointment.getMedicalTests() != null && !appointment.getMedicalTests().isEmpty()) {
            appointment.getMedicalTests().forEach(medicalTest -> {
                medicalTest.setAppointment(null);
                medicalTestRepository.save(medicalTest);
            });
        }

        // Desvinculamos el slot de la cita
        AppointmentSlotEntity appointmentSlot = appointment.getSlot();
        appointmentSlot.setAppointment(null);
        appointmentSlotRepository.save(appointmentSlot);

        // Finalmente, eliminamos la cita
        appointmentRepository.delete(appointment);
    }

    @Transactional(readOnly = true)
    public boolean canUserDeleteAppointment(Integer appointmentId, UserEntity user) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow(DataNotFoundException::appointmentNotFound);

        // Comprobamos que la cita pertenece al usuario
        if (!appointment.getPatient().getId().equals(user.getId())) {
            return false;
        }

        // Comprobamos que no falten menos de 24 horas para la cita y devolvemos
        return appointment.getSlot().getDate().isAfter(LocalDate.now().plusDays(1));
    }

    @Transactional(readOnly = true)
    public boolean canUserAccessAppointment(Integer appointmentId, UserEntity user) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow(DataNotFoundException::appointmentNotFound);

        // Comprobamos que la cita pertenece al usuario
        return appointment.getPatient().getId().equals(user.getId());
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