package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.DoctorScheduleDTO;
import com.kikisito.salus.api.dto.request.DoctorScheduleRequest;
import com.kikisito.salus.api.entity.DoctorScheduleEntity;
import com.kikisito.salus.api.entity.RoomEntity;
import com.kikisito.salus.api.entity.SpecialtyEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.DoctorScheduleRepository;
import com.kikisito.salus.api.repository.RoomRepository;
import com.kikisito.salus.api.repository.SpecialtyRepository;
import com.kikisito.salus.api.repository.MedicalProfileRepository;
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
public class DoctorScheduleService {
    @Autowired
    private final DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private final MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private final SpecialtyRepository specialtyRepository;

    @Autowired
    private final RoomRepository roomRepository;

    @Autowired
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<DoctorScheduleDTO> getDoctorSchedules(Integer doctorId) {
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);
        List<DoctorScheduleEntity> schedules = doctorScheduleRepository.findByDoctor(doctor);
        return schedules.stream().map(agenda -> modelMapper.map(agenda, DoctorScheduleDTO.class)).toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleDTO> getDoctorSchedulesByDayOfWeek(Integer doctorId, DayOfWeek dayOfWeek) {
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);
        List<DoctorScheduleEntity> schedules = doctorScheduleRepository.findByDoctorAndDayOfWeek(doctor, dayOfWeek);
        return schedules.stream().map(agenda -> modelMapper.map(agenda, DoctorScheduleDTO.class)).toList();
    }

    @Transactional
    public DoctorScheduleDTO addSchedule(DoctorScheduleRequest doctorScheduleRequest) {
        // Buscamos si existe el perfil del médico y lo recuperamos
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorScheduleRequest.getDoctor()).orElseThrow(DataNotFoundException::doctorNotFound);

        // Buscamos la especialidad y la consulta
        SpecialtyEntity specialty = specialtyRepository.findById(doctorScheduleRequest.getSpecialty()).orElseThrow(DataNotFoundException::specialtyNotFound);

        // Comprobamos si el médico tiene la especialidad de la petición
        if(!doctor.getSpecialties().contains(specialty)) {
            throw ConflictException.doctorDoesNotHaveSpecialty();
        }

        // Buscamos la consulta
        RoomEntity room = roomRepository.findById(doctorScheduleRequest.getRoom()).orElseThrow(DataNotFoundException::roomNotFound);

        // Obtenemos todas las agendas existentes para el médico y el día de la semana
        List<DoctorScheduleEntity> existingSchedules = doctorScheduleRepository.findByDoctorAndDayOfWeek(doctor, doctorScheduleRequest.getDayOfWeek());

        // Obtenemos los horarios de la nueva
        DayOfWeek dayOfWeek = doctorScheduleRequest.getDayOfWeek();
        LocalTime start1 = doctorScheduleRequest.getStartTime();
        LocalTime end1 = doctorScheduleRequest.getEndTime();

        // Verificamos si hay colisión de horarios
        boolean scheduleConflict = this.agendaOverlap(dayOfWeek, start1, end1, existingSchedules);

        if (scheduleConflict) {
            throw ConflictException.scheduleConflict();
        }

        // Mapeamos la petición a la entidad y guardamos
        DoctorScheduleEntity doctorScheduleEntity = modelMapper.map(doctorScheduleRequest, DoctorScheduleEntity.class);
        doctorScheduleEntity.setDoctor(doctor);
        doctorScheduleEntity.setSpecialty(specialty);
        doctorScheduleEntity.setRoom(room);
        doctorScheduleEntity = doctorScheduleRepository.save(doctorScheduleEntity);

        // Mapeamos la entidad a DTO y retornamos
        return modelMapper.map(doctorScheduleEntity, DoctorScheduleDTO.class);
    }

    @Transactional
    public DoctorScheduleDTO updateSchedule(Integer scheduleId, DoctorScheduleRequest doctorScheduleRequest) {
        // Buscamos la agenda a actualizar
        DoctorScheduleEntity schedule = doctorScheduleRepository.findById(scheduleId).orElseThrow(DataNotFoundException::scheduleNotFound);

        // Buscamos si existe el perfil del médico y lo recuperamos
        MedicalProfileEntity doctor = medicalProfileRepository.findById(schedule.getDoctor().getId()).orElseThrow(DataNotFoundException::doctorNotFound);

        // Buscamos la especialidad y la consulta
        SpecialtyEntity specialty = specialtyRepository.findById(doctorScheduleRequest.getSpecialty()).orElseThrow(DataNotFoundException::specialtyNotFound);

        // Comprobamos si el médico tiene la especialidad de la petición
        if(!doctor.getSpecialties().contains(specialty)) {
            throw ConflictException.doctorDoesNotHaveSpecialty();
        }

        // Buscamos la consulta
        RoomEntity consulta = roomRepository.findById(doctorScheduleRequest.getRoom()).orElseThrow(DataNotFoundException::roomNotFound);

        // Obtenemos todas las agendas existentes para el médico y el día de la semana
        List<DoctorScheduleEntity> existingSchedules = doctorScheduleRepository.findByDoctorAndDayOfWeek(schedule.getDoctor(), schedule.getDayOfWeek());

        // Obtenemos los horarios de la nueva
        DayOfWeek dayOfWeek = doctorScheduleRequest.getDayOfWeek();
        LocalTime start1 = doctorScheduleRequest.getStartTime();
        LocalTime end1 = doctorScheduleRequest.getEndTime();

        // Verificamos si hay colisión de horarios
        boolean scheduleConflict = this.agendaOverlap(scheduleId, dayOfWeek, start1, end1, existingSchedules);

        if (scheduleConflict) {
            throw ConflictException.scheduleConflict();
        }

        // Mapeamos la petición a la entidad y guardamos
        schedule.setSpecialty(specialty);
        schedule.setRoom(consulta);
        schedule.setDayOfWeek(doctorScheduleRequest.getDayOfWeek());
        schedule.setStartTime(doctorScheduleRequest.getStartTime());
        schedule.setEndTime(doctorScheduleRequest.getEndTime());
        schedule.setDuration(doctorScheduleRequest.getDuration());
        schedule = doctorScheduleRepository.save(schedule);

        // Mapeamos la entidad a DTO y retornamos
        return modelMapper.map(schedule, DoctorScheduleDTO.class);
    }

    @Transactional
    public void deleteSchedule(Integer scheduleId) {
        if (!doctorScheduleRepository.existsById(scheduleId)) {
            throw DataNotFoundException.scheduleNotFound();
        }
        doctorScheduleRepository.deleteById(scheduleId);
    }

    private boolean agendaOverlap(DayOfWeek dayOfWeek, LocalTime start1, LocalTime end1, List<DoctorScheduleEntity> schedules) {
        return this.agendaOverlap(null, dayOfWeek, start1, end1, schedules);
    }

    private boolean agendaOverlap(Integer skipEntry, DayOfWeek dayOfWeek, LocalTime start1, LocalTime end1, List<DoctorScheduleEntity> schedules) {
        return schedules.stream().anyMatch(
                agenda -> {
                    if (agenda.getDayOfWeek() != dayOfWeek) {
                        return false;
                    }

                    // Si la agenda es la misma que la que se está actualizando, no se verifica
                    if (agenda.getId().equals(skipEntry)) {
                        return false;
                    }

                    LocalTime start2 = agenda.getStartTime();
                    LocalTime end2 = agenda.getEndTime();
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