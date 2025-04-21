package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.AppointmentSlotDTO;
import com.kikisito.salus.api.dto.request.AppointmentSlotRequest;
import com.kikisito.salus.api.entity.AppointmentSlotEntity;
import com.kikisito.salus.api.entity.RoomEntity;
import com.kikisito.salus.api.entity.SpecialtyEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentSlotService {
    @Autowired
    private final AppointmentSlotRepository appointmentSlotRepository;

    @Autowired
    private final DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private final AusenciaMedicoRepository ausenciaMedicoRepository;

    @Autowired
    private final MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private RoomRepository roomRepository;

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
    public List<AppointmentSlotDTO> getAppointmentSlotsByDoctorAndDate(Integer doctorId, LocalDate date) {
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);
        List<AppointmentSlotEntity> appointmentSlots = appointmentSlotRepository.findByDoctorAndDate(doctor, date);

        return appointmentSlots.stream()
                .map(appointmentSlot -> modelMapper.map(appointmentSlot, AppointmentSlotDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentSlotDTO> getWeeklyAppointmentSlotsByDoctorAndDate(Integer doctorId, LocalDate date) {
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        // Calcula cuándo empieza y termina la semana a la que pertenece la fecha dada
        LocalDate startDate = date.minusDays(date.getDayOfWeek().getValue() - 1);
        LocalDate endDate = startDate.plusDays(6);

        // Recupera los huecos de la semana
        List<AppointmentSlotEntity> appointmentSlots = appointmentSlotRepository.findByDoctorAndDateBetween(doctor, startDate, endDate);

        // Mapea los huecos a DTOs
        return appointmentSlots.stream()
                .map(appointmentSlot -> modelMapper.map(appointmentSlot, AppointmentSlotDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentSlotDTO getAppointmentSlot(Integer appointmentSlotId) {
        AppointmentSlotEntity appointmentSlot = appointmentSlotRepository.findById(appointmentSlotId).orElseThrow(DataNotFoundException::appointmentSlotNotFound);
        return modelMapper.map(appointmentSlot, AppointmentSlotDTO.class);
    }

    @Transactional
    public AppointmentSlotDTO createAppointmentSlot(AppointmentSlotRequest appointmentSlotRequest) {
        // Recuperamos el medico, la especialidad y la consulta de la base de datos
        MedicalProfileEntity doctor = medicalProfileRepository.findById(appointmentSlotRequest.getDoctor()).orElseThrow(DataNotFoundException::doctorNotFound);
        SpecialtyEntity specialty = specialtyRepository.findById(appointmentSlotRequest.getSpecialty()).orElseThrow(DataNotFoundException::specialtyNotFound);
        RoomEntity room = roomRepository.findById(appointmentSlotRequest.getRoom()).orElseThrow(DataNotFoundException::roomNotFound);

        // Comprobamos que no haya conflictos con otros horarios del médico
        List<AppointmentSlotEntity> appointmentSlots = appointmentSlotRepository.findByDoctorAndDate(doctor, appointmentSlotRequest.getDate());
        for (AppointmentSlotEntity dbAppointmentSlot : appointmentSlots) {
            if (timesOverlap(appointmentSlotRequest.getStartTime(), appointmentSlotRequest.getEndTime(), dbAppointmentSlot.getStartTime(), dbAppointmentSlot.getEndTime())) {
                throw ConflictException.scheduleConflict();
            }
        }

        // Comprobamos que no haya conflictos con otros horarios de la consulta
        List<AppointmentSlotEntity> roomAppointmentSlots = appointmentSlotRepository.findByRoomAndDate(room, appointmentSlotRequest.getDate());
        for (AppointmentSlotEntity dbAppointmentSlot : roomAppointmentSlots) {
            if (timesOverlap(appointmentSlotRequest.getStartTime(), appointmentSlotRequest.getEndTime(), dbAppointmentSlot.getStartTime(), dbAppointmentSlot.getEndTime())) {
                throw ConflictException.scheduleConflict();
            }
        }

        // Creamos la CitaSlot
        AppointmentSlotEntity appointmentSlotEntity = AppointmentSlotEntity.builder()
                .doctor(doctor)
                .specialty(specialty)
                .room(room)
                .date(appointmentSlotRequest.getDate())
                .startTime(appointmentSlotRequest.getStartTime())
                .endTime(appointmentSlotRequest.getEndTime())
                .build();

        // Guardamos la CitaSlot en la base de datos
        appointmentSlotEntity = appointmentSlotRepository.save(appointmentSlotEntity);

        // Devolvemos la CitaSlot
        return modelMapper.map(appointmentSlotEntity, AppointmentSlotDTO.class);
    }

    @Transactional
    public void deleteAppointmentSlot(Integer id) {
        if(!appointmentSlotRepository.existsById(id)) {
            throw DataNotFoundException.appointmentSlotNotFound();
        }

        appointmentSlotRepository.deleteById(id);
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