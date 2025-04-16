package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.AppointmentSlotDTO;
import com.kikisito.salus.api.dto.request.AppointmentSlotRequest;
import com.kikisito.salus.api.entity.AppointmentSlotEntity;
import com.kikisito.salus.api.entity.RoomEntity;
import com.kikisito.salus.api.entity.SpecialtyEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
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
        MedicalProfileEntity medico = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);
        return appointmentSlotRepository.findByDoctorAndDate(medico, date);
    }

    @Transactional
    public AppointmentSlotDTO createAppointmentSlot(AppointmentSlotRequest appointmentSlotRequest) {
        // Recuperamos el medico, la especialidad y la consulta de la base de datos
        MedicalProfileEntity doctor = medicalProfileRepository.findById(appointmentSlotRequest.getDoctor()).orElseThrow(DataNotFoundException::doctorNotFound);
        SpecialtyEntity specialty = specialtyRepository.findById(appointmentSlotRequest.getSpecialty()).orElseThrow(DataNotFoundException::specialtyNotFound);
        RoomEntity room = roomRepository.findById(appointmentSlotRequest.getRoom()).orElseThrow(DataNotFoundException::roomNotFound);

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