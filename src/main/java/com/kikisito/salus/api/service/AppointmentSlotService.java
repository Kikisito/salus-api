package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.AppointmentSlotDTO;
import com.kikisito.salus.api.dto.request.AppointmentSlotRequest;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.exception.BadRequestException;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private final MedicalCenterRepository medicalCenterRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public List<AppointmentSlotDTO> generateAppointmentSlotsBetweenDates(LocalDate startDate, LocalDate endDate) {
        // Obtenemos todos los doctores registrados para generar los huecos de citas
        List<MedicalProfileEntity> doctors = medicalProfileRepository.findAll();

        // Generamos las citas
        List<AppointmentSlotDTO> appointmentSlots = new ArrayList<>();
        for(MedicalProfileEntity doctor : doctors) {
            List<AppointmentSlotDTO> dateSlots = this.generateAppointmentSlotsByDoctorBetweenDates(doctor.getId(), startDate, endDate);
            appointmentSlots.addAll(dateSlots);
        }

        return appointmentSlots;
    }

    // Genera los huecos de citas para un médico en un rango de fechas
    @Transactional
    public List<AppointmentSlotDTO> generateAppointmentSlotsByDoctorBetweenDates(Integer doctorId, LocalDate startDate, LocalDate endDate) {
        // Comprobamos que las fechas introducidas no estén en el pasado y que la fecha de fin no sea anterior a la de inicio
        if(startDate.isBefore(LocalDate.now()) || endDate.isBefore(LocalDate.now()) || endDate.isBefore(startDate)) {
            throw BadRequestException.invalidDateOrDateRange();
        }

        // Comprobamos que el médico existe
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        List<AppointmentSlotDTO> appointmentSlots = new ArrayList<>();
        for(LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // Obtiene los turnos del médico para la fecha dada
            List<DoctorScheduleEntity> schedules = doctorScheduleRepository.findByDoctorAndDayOfWeek(doctor, date.getDayOfWeek());

            // Genera los huecos de citas para cada turno del día
            for(DoctorScheduleEntity schedule : schedules) {
                List<AppointmentSlotDTO> dateSlots = this.generateAppointmentSlotsByScheduleId(schedule.getId(), date);
                appointmentSlots.addAll(dateSlots);
            }
        }

        return appointmentSlots;
    }

    // Genera los huecos de citas de un turno específico para una fecha dada
    // El día de la semana de la petición DEBE coincidir con el día de la semana del turno registrado
    @Transactional
    public List<AppointmentSlotDTO> generateAppointmentSlotsByScheduleId(Integer scheduleId, LocalDate date) {
        // Comprobamos que la fecha introducida no esté en el pasado
        if(date.isBefore(LocalDate.now())) {
            throw BadRequestException.invalidDateOrDateRange();
        }

        // Obtenemos el turno especificado
        DoctorScheduleEntity schedule = doctorScheduleRepository.findById(scheduleId).orElseThrow(DataNotFoundException::scheduleNotFound);

        // Comprobamos que el día de la semana de la petición coincida con el del turno
        if(!schedule.getDayOfWeek().equals(date.getDayOfWeek())) {
            throw ConflictException.dayMismatch();
        }

        // Datos básicos del turno
        LocalTime startTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        Integer duration = schedule.getDuration();

        List<AppointmentSlotEntity> generatedSlots = new ArrayList<>();
        for(LocalTime time = startTime; time.isBefore(endTime); time = time.plusMinutes(duration)) {
            // Comprobamos que no solapa con otros horarios en la misma consulta
            RoomEntity room = schedule.getRoom();

            // Comprobamos que no haya conflictos con otros horarios de la consulta
            boolean roomConflict = appointmentSlotRepository.existsRoomOverlappingSlot(room, date, startTime, endTime);

            // Comprobamos que no haya conflictos con otros horarios del médico
            boolean doctorConflict = appointmentSlotRepository.existsDoctorOverlappingSlot(schedule.getDoctor(), date, startTime, endTime);

            if(roomConflict || doctorConflict) {
                throw ConflictException.scheduleConflict();
            }

            // Creamos un nuevo AppointmentSlotEntity
            AppointmentSlotEntity appointmentSlot = AppointmentSlotEntity.builder()
                    .doctor(schedule.getDoctor())
                    .specialty(schedule.getSpecialty())
                    .room(schedule.getRoom())
                    .date(date)
                    .startTime(time)
                    .endTime(time.plusMinutes(duration))
                    .build();

            // Añadimos el AppointmentSlotEntity a la lista
            generatedSlots.add(appointmentSlot);
        }

        // Guardamos los AppointmentSlotEntity en la base de datos
        List<AppointmentSlotEntity> savedSlots = appointmentSlotRepository.saveAll(generatedSlots);

        // Mapeamos los DTO y devolvemos
        return savedSlots.stream()
                .map(appointmentSlot -> modelMapper.map(appointmentSlot, AppointmentSlotDTO.class))
                .toList();
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

    @Transactional(readOnly = true)
    public List<AppointmentSlotDTO> getAvailableDatesByDoctorAndMedicalCenterAndSpecialty(Integer medicalCenterId, Integer specialtyId, Integer doctorId) {
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);
        SpecialtyEntity specialty = specialtyRepository.findById(specialtyId).orElseThrow(DataNotFoundException::specialtyNotFound);
        MedicalCenterEntity medicalCenter = medicalCenterRepository.findById(medicalCenterId).orElseThrow(DataNotFoundException::medicalCenterNotFound);

        // Recuperamos los huecos de la semana
        List<AppointmentSlotEntity> slots = appointmentSlotRepository.findAvailableDatesByDoctorAndMedicalCenterAndSpecialty(medicalCenter, specialty, doctor);

        // Mapeo de los slots a DTOs y devolvemos la lista
        return slots.stream()
                .map(appointmentSlot -> modelMapper.map(appointmentSlot, AppointmentSlotDTO.class))
                .toList();
    }

    @Transactional
    public AppointmentSlotDTO createAppointmentSlot(AppointmentSlotRequest appointmentSlotRequest) {
        // Recuperamos el medico, la especialidad y la consulta de la base de datos
        MedicalProfileEntity doctor = medicalProfileRepository.findById(appointmentSlotRequest.getDoctor()).orElseThrow(DataNotFoundException::doctorNotFound);
        SpecialtyEntity specialty = specialtyRepository.findById(appointmentSlotRequest.getSpecialty()).orElseThrow(DataNotFoundException::specialtyNotFound);
        RoomEntity room = roomRepository.findById(appointmentSlotRequest.getRoom()).orElseThrow(DataNotFoundException::roomNotFound);

        // Comprobamos que no haya conflictos con otros horarios
        boolean roomConflict = appointmentSlotRepository.existsRoomOverlappingSlot(room, appointmentSlotRequest.getDate(), appointmentSlotRequest.getStartTime(), appointmentSlotRequest.getEndTime());
        boolean doctorConflict = appointmentSlotRepository.existsDoctorOverlappingSlot(doctor, appointmentSlotRequest.getDate(), appointmentSlotRequest.getStartTime(), appointmentSlotRequest.getEndTime());
        if(roomConflict || doctorConflict) {
            throw ConflictException.scheduleConflict();
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
}