package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.DoctorScheduleDTO;
import com.kikisito.salus.api.dto.request.DoctorScheduleRequest;
import com.kikisito.salus.api.entity.DoctorScheduleEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.RoomEntity;
import com.kikisito.salus.api.entity.SpecialtyEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.DoctorScheduleRepository;
import com.kikisito.salus.api.repository.MedicalProfileRepository;
import com.kikisito.salus.api.repository.RoomRepository;
import com.kikisito.salus.api.repository.SpecialtyRepository;
import com.kikisito.salus.api.type.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DoctorScheduleServiceTest {

    @MockitoBean
    private DoctorScheduleRepository doctorScheduleRepository;

    @MockitoBean
    private MedicalProfileRepository medicalProfileRepository;

    @MockitoBean
    private SpecialtyRepository specialtyRepository;

    @MockitoBean
    private RoomRepository roomRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    private MedicalProfileEntity testDoctor;
    private SpecialtyEntity testSpecialty;
    private SpecialtyEntity testSpecialty2;
    private RoomEntity testRoom;
    private DoctorScheduleEntity testSchedule;
    private DoctorScheduleDTO testScheduleDTO;
    private DoctorScheduleRequest testScheduleRequest;

    @BeforeEach
    void setUp() {
        // Usuario de prueba
        UserEntity testUser = UserEntity.builder()
                .id(1)
                .nombre("Hugo")
                .apellidos("Herrera")
                .email("hugo@salus.com")
                .nif("12345678A")
                .rolesList(new ArrayList<>(List.of(RoleType.USER, RoleType.PROFESSIONAL)))
                .build();

        // Especialidades de prueba
        testSpecialty = SpecialtyEntity.builder()
                .id(1)
                .name("Medicina General")
                .description("Especialidad de medicina general")
                .build();

        testSpecialty2 = SpecialtyEntity.builder()
                .id(2)
                .name("Cardiología")
                .description("Especialidad de cardiología")
                .build();

        // Doctor de prueba
        testDoctor = MedicalProfileEntity.builder()
                .id(1)
                .user(testUser)
                .license("COLEG-TEST-1")
                .specialties(new ArrayList<>(Collections.singletonList(testSpecialty)))
                .build();

        // Consulta de prueba
        testRoom = RoomEntity.builder()
                .id(1)
                .name("Consulta 1")
                .build();

        // Horario de prueba
        testSchedule = DoctorScheduleEntity.builder()
                .id(1)
                .doctor(testDoctor)
                .specialty(testSpecialty)
                .room(testRoom)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(14, 0))
                .duration(30)
                .build();

        testScheduleDTO = modelMapper.map(testSchedule, DoctorScheduleDTO.class);

        // Request de horario de prueba
        testScheduleRequest = DoctorScheduleRequest.builder()
                .doctor(1)
                .specialty(1)
                .room(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(14, 0))
                .duration(30)
                .build();
    }

    @Test
    void C1_getDoctorSchedules_should_return_one_schedule() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(doctorScheduleRepository.findByDoctor(testDoctor)).thenReturn(Collections.singletonList(testSchedule));

        // Act
        List<DoctorScheduleDTO> result = doctorScheduleService.getDoctorSchedules(1);

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testScheduleDTO.getId(), result.getFirst().getId());
            assertEquals(testScheduleDTO.getDayOfWeek(), result.getFirst().getDayOfWeek());
            assertEquals(testScheduleDTO.getStartTime(), result.getFirst().getStartTime());
            assertEquals(testScheduleDTO.getEndTime(), result.getFirst().getEndTime());
            assertEquals(testScheduleDTO.getDuration(), result.getFirst().getDuration());
        });

        verify(medicalProfileRepository).findById(1);
        verify(doctorScheduleRepository).findByDoctor(testDoctor);
    }

    @Test
    void C2_getDoctorSchedulesByDayOfWeek_should_return_one_schedule() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(doctorScheduleRepository.findByDoctorAndDayOfWeek(testDoctor, DayOfWeek.MONDAY)).thenReturn(Collections.singletonList(testSchedule));

        // Act
        List<DoctorScheduleDTO> result = assertDoesNotThrow(() -> doctorScheduleService.getDoctorSchedulesByDayOfWeek(1, DayOfWeek.MONDAY));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testScheduleDTO.getId(), result.getFirst().getId());
            assertEquals(testScheduleDTO.getDayOfWeek(), result.getFirst().getDayOfWeek());
            assertEquals(testScheduleDTO.getStartTime(), result.getFirst().getStartTime());
            assertEquals(testScheduleDTO.getEndTime(), result.getFirst().getEndTime());
            assertEquals(testScheduleDTO.getDuration(), result.getFirst().getDuration());
        });

        verify(medicalProfileRepository).findById(1);
        verify(doctorScheduleRepository).findByDoctorAndDayOfWeek(testDoctor, DayOfWeek.MONDAY);
    }

    @Test
    void C3_getDoctorSchedules_should_throw_exception_when_doctor_not_found() {
        // Arrange
        when(medicalProfileRepository.findById(3)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> doctorScheduleService.getDoctorSchedules(3));

        // Assert
        assertEquals("data_not_found.doctor", exception.getCode());

        verify(medicalProfileRepository).findById(3);
        verify(doctorScheduleRepository, never()).findByDoctor(any(MedicalProfileEntity.class));
    }

    @Test
    void C4_addSchedule_should_return_the_created_schedule() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(roomRepository.findById(1)).thenReturn(Optional.of(testRoom));
        when(doctorScheduleRepository.findByDoctorAndDayOfWeek(testDoctor, DayOfWeek.MONDAY)).thenReturn(new ArrayList<>());
        when(doctorScheduleRepository.save(any(DoctorScheduleEntity.class))).thenReturn(testSchedule);

        // Act
        DoctorScheduleDTO result = assertDoesNotThrow(() -> doctorScheduleService.addSchedule(testScheduleRequest));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testScheduleDTO.getId(), result.getId());
            assertEquals(testScheduleDTO.getDayOfWeek(), result.getDayOfWeek());
            assertEquals(testScheduleDTO.getStartTime(), result.getStartTime());
            assertEquals(testScheduleDTO.getEndTime(), result.getEndTime());
            assertEquals(testScheduleDTO.getDuration(), result.getDuration());
        });

        verify(medicalProfileRepository).findById(1);
        verify(specialtyRepository).findById(1);
        verify(roomRepository).findById(1);
        verify(doctorScheduleRepository).findByDoctorAndDayOfWeek(testDoctor, DayOfWeek.MONDAY);
        verify(doctorScheduleRepository).save(any(DoctorScheduleEntity.class));
    }

    @Test
    void C5_addSchedule_should_throw_exception_when_doctor_not_found() {
        // Arrange
        when(medicalProfileRepository.findById(3)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> doctorScheduleService.addSchedule(
                        DoctorScheduleRequest.builder()
                                .doctor(3)
                                .specialty(1)
                                .room(1)
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .startTime(LocalTime.of(9, 0))
                                .endTime(LocalTime.of(14, 0))
                                .duration(30)
                                .build()
                ));

        // Assert
        assertEquals("data_not_found.doctor", exception.getCode());

        verify(medicalProfileRepository).findById(3);
        verify(specialtyRepository, never()).findById(anyInt());
        verify(roomRepository, never()).findById(anyInt());
        verify(doctorScheduleRepository, never()).save(any(DoctorScheduleEntity.class));
    }

    @Test
    void C6_addSchedule_should_throw_exception_when_doctor_does_not_have_specialty() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(specialtyRepository.findById(2)).thenReturn(Optional.of(testSpecialty2));

        DoctorScheduleRequest request = DoctorScheduleRequest.builder()
                .doctor(1)
                .specialty(2)
                .room(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(14, 0))
                .duration(30)
                .build();

        // Act
        ConflictException exception = assertThrows(ConflictException.class, () -> doctorScheduleService.addSchedule(request));

        // Assert
        assertEquals("conflict.doctor_does_not_have_specialty", exception.getCode());

        verify(medicalProfileRepository).findById(1);
        verify(specialtyRepository).findById(2);
        verify(roomRepository, never()).findById(anyInt());
        verify(doctorScheduleRepository, never()).save(any(DoctorScheduleEntity.class));
    }

    @Test
    void C7_addSchedule_should_throw_exception_when_schedule_conflicts() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(roomRepository.findById(1)).thenReturn(Optional.of(testRoom));
        when(doctorScheduleRepository.findByDoctorAndDayOfWeek(testDoctor, DayOfWeek.MONDAY))
                .thenReturn(Collections.singletonList(testSchedule));

        DoctorScheduleRequest request = DoctorScheduleRequest.builder()
                .doctor(1)
                .specialty(1)
                .room(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(15, 0))
                .duration(30)
                .build();

        // Act
        ConflictException exception = assertThrows(ConflictException.class, () -> doctorScheduleService.addSchedule(request));

        // Assert
        assertEquals("conflict.schedule_conflict", exception.getCode());

        verify(medicalProfileRepository).findById(1);
        verify(specialtyRepository).findById(1);
        verify(roomRepository).findById(1);
        verify(doctorScheduleRepository).findByDoctorAndDayOfWeek(testDoctor, DayOfWeek.MONDAY);
        verify(doctorScheduleRepository, never()).save(any(DoctorScheduleEntity.class));
    }

    @Test
    void C8_updateSchedule_should_return_the_updated_schedule() {
        // Arrange
        DoctorScheduleRequest updateRequest = DoctorScheduleRequest.builder()
                .specialty(1)
                .room(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(15, 0))
                .duration(30)
                .build();

        DoctorScheduleEntity updatedSchedule = DoctorScheduleEntity.builder()
                .id(1)
                .doctor(testDoctor)
                .specialty(testSpecialty)
                .room(testRoom)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(15, 0))
                .duration(30)
                .build();

        when(doctorScheduleRepository.findById(1)).thenReturn(Optional.of(testSchedule));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(roomRepository.findById(1)).thenReturn(Optional.of(testRoom));
        when(doctorScheduleRepository.findByDoctorAndDayOfWeek(testDoctor, DayOfWeek.MONDAY)).thenReturn(Collections.singletonList(testSchedule));
        when(doctorScheduleRepository.save(any(DoctorScheduleEntity.class))).thenReturn(updatedSchedule);

        // Act
        DoctorScheduleDTO result = assertDoesNotThrow(() -> doctorScheduleService.updateSchedule(1, updateRequest));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
            assertEquals(LocalTime.of(10, 0), result.getStartTime());
            assertEquals(LocalTime.of(15, 0), result.getEndTime());
            assertEquals(30, result.getDuration());
        });

        verify(doctorScheduleRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(specialtyRepository).findById(1);
        verify(roomRepository).findById(1);
        verify(doctorScheduleRepository).findByDoctorAndDayOfWeek(testDoctor, DayOfWeek.MONDAY);
        verify(doctorScheduleRepository).save(any(DoctorScheduleEntity.class));
    }

    @Test
    void C9_deleteSchedule_should_delete_the_schedule() {
        // Arrange
        when(doctorScheduleRepository.existsById(1)).thenReturn(true);
        doNothing().when(doctorScheduleRepository).deleteById(1);

        // Act
        assertDoesNotThrow(() -> doctorScheduleService.deleteSchedule(1));

        // Assert
        verify(doctorScheduleRepository).existsById(1);
        verify(doctorScheduleRepository).deleteById(1);
    }

    @Test
    void C10_deleteSchedule_should_throw_exception_when_schedule_not_found() {
        // Arrange
        when(doctorScheduleRepository.existsById(3)).thenReturn(false);

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> doctorScheduleService.deleteSchedule(3));

        // Assert
        assertEquals("data_not_found.schedule", exception.getCode());

        verify(doctorScheduleRepository).existsById(3);
        verify(doctorScheduleRepository, never()).deleteById(anyInt());
    }
}