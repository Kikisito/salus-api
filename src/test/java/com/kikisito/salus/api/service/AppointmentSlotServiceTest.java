package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.AppointmentSlotDTO;
import com.kikisito.salus.api.dto.request.AppointmentSlotRequest;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import com.kikisito.salus.api.type.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AppointmentSlotServiceTest {

    @MockitoBean
    private AppointmentSlotRepository appointmentSlotRepository;

    @MockitoBean
    private DoctorScheduleRepository doctorScheduleRepository;

    @MockitoBean
    private MedicalProfileRepository medicalProfileRepository;

    @MockitoBean
    private MedicalCenterRepository medicalCenterRepository;

    @MockitoBean
    private SpecialtyRepository specialtyRepository;

    @MockitoBean
    private RoomRepository roomRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AppointmentSlotService appointmentSlotService;

    private MedicalProfileEntity testDoctor;
    private SpecialtyEntity testSpecialty;
    private MedicalCenterEntity testMedicalCenter;
    private RoomEntity testRoom;
    private DoctorScheduleEntity testSchedule;
    private AppointmentSlotEntity testAppointmentSlot;
    private AppointmentSlotDTO testAppointmentSlotDTO;
    private AppointmentSlotRequest testAppointmentSlotRequest;
    private List<AppointmentSlotEntity> testAppointmentSlots;

    @BeforeEach
    void setUp() {
        // Usuario de prueba
        UserEntity testUser = UserEntity.builder()
                .id(1)
                .nombre("Gerardo")
                .apellidos("González")
                .email("doctor@salus.com")
                .nif("12345678A")
                .rolesList(new ArrayList<>(List.of(RoleType.PROFESSIONAL)))
                .build();

        // Especialidad de prueba
        testSpecialty = SpecialtyEntity.builder()
                .id(1)
                .name("Medicina General")
                .description("Especialidad de medicina general")
                .build();

        // Doctor de prueba
        testDoctor = MedicalProfileEntity.builder()
                .id(1)
                .user(testUser)
                .license("COLEG-TEST-1")
                .specialties(new ArrayList<>(Collections.singletonList(testSpecialty)))
                .build();

        // Centro médico de prueba
        testMedicalCenter = MedicalCenterEntity.builder()
                .id(1)
                .name("Hospital de Pruebas")
                .email("test@salus.com")
                .phone("632498123")
                .addressLine1("Calle de Pruebas 3")
                .zipCode("03001")
                .country("España")
                .province("Alicante")
                .municipality("Alicante")
                .locality("Alicante")
                .build();

        // Consulta de prueba
        testRoom = RoomEntity.builder()
                .id(1)
                .name("Consulta 1")
                .medicalCenter(testMedicalCenter)
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

        // Slot de cita de prueba
        testAppointmentSlot = AppointmentSlotEntity.builder()
                .id(1)
                .doctor(testDoctor)
                .specialty(testSpecialty)
                .room(testRoom)
                .date(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .build();

        testAppointmentSlotDTO = modelMapper.map(testAppointmentSlot, AppointmentSlotDTO.class);

        // Lista de slots de cita
        testAppointmentSlots = new ArrayList<>(List.of(testAppointmentSlot));

        // Request para crear slot de cita
        testAppointmentSlotRequest = AppointmentSlotRequest.builder()
                .doctor(1)
                .specialty(1)
                .room(1)
                .date(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .build();
    }

    @Test
    void C1_getAppointmentSlot_should_return_appointment_slot_when_it_exists() {
        // Arrange
        when(appointmentSlotRepository.findById(1)).thenReturn(Optional.of(testAppointmentSlot));

        // Act
        AppointmentSlotDTO result = assertDoesNotThrow(() -> appointmentSlotService.getAppointmentSlot(1));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testAppointmentSlotDTO.getId(), result.getId());
            assertEquals(testAppointmentSlotDTO.getDoctor().getId(), result.getDoctor().getId());
            assertEquals(testAppointmentSlotDTO.getSpecialty().getId(), result.getSpecialty().getId());
            assertEquals(testAppointmentSlotDTO.getRoom().getId(), result.getRoom().getId());
            assertEquals(testAppointmentSlotDTO.getDate(), result.getDate());
            assertEquals(testAppointmentSlotDTO.getStartTime(), result.getStartTime());
            assertEquals(testAppointmentSlotDTO.getEndTime(), result.getEndTime());
        });

        verify(appointmentSlotRepository).findById(1);
    }

    @Test
    void C2_getAppointmentSlot_should_throw_exception_when_appointment_slot_does_not_exist() {
        // Arrange
        when(appointmentSlotRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> appointmentSlotService.getAppointmentSlot(1));

        // Assert
        assertAll(() -> {
            assertNotNull(exception);
            assertEquals("data_not_found.appointment_slot", exception.getCode());
        });

        verify(appointmentSlotRepository).findById(1);
    }

    @Test
    void C3_getAppointmentSlotsByDoctorAndDate_should_return_list_of_appointment_slots() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(7);
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(appointmentSlotRepository.findByDoctorAndDate(testDoctor, testDate)).thenReturn(testAppointmentSlots);

        // Act
        List<AppointmentSlotDTO> result = assertDoesNotThrow(() -> appointmentSlotService.getAppointmentSlotsByDoctorAndDate(1, testDate));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testAppointmentSlotDTO.getId(), result.getFirst().getId());
            assertEquals(testAppointmentSlotDTO.getDate(), result.getFirst().getDate());
            assertEquals(testAppointmentSlotDTO.getStartTime(), result.getFirst().getStartTime());
        });

        verify(medicalProfileRepository).findById(1);
        verify(appointmentSlotRepository).findByDoctorAndDate(testDoctor, testDate);
    }

    @Test
    void C4_getWeeklyAppointmentSlotsByDoctorAndDate_should_return_list_of_appointment_slots() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(2);
        LocalDate startDate = testDate.minusDays(testDate.getDayOfWeek().getValue() - 1);
        LocalDate endDate = startDate.plusDays(6);

        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(appointmentSlotRepository.findByDoctorAndDateBetween(testDoctor, startDate, endDate)).thenReturn(testAppointmentSlots);

        // Act
        List<AppointmentSlotDTO> result = assertDoesNotThrow(() -> appointmentSlotService.getWeeklyAppointmentSlotsByDoctorAndDate(1, testDate));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testAppointmentSlotDTO.getId(), result.getFirst().getId());
        });

        verify(medicalProfileRepository).findById(1);
        verify(appointmentSlotRepository).findByDoctorAndDateBetween(testDoctor, startDate, endDate);
    }

    @Test
    void C5_createAppointmentSlot_should_return_created_appointment_slot() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(roomRepository.findById(1)).thenReturn(Optional.of(testRoom));
        when(appointmentSlotRepository.existsRoomOverlappingSlot(any(), any(), any(), any())).thenReturn(false);
        when(appointmentSlotRepository.existsDoctorOverlappingSlot(any(), any(), any(), any())).thenReturn(false);
        when(appointmentSlotRepository.save(any(AppointmentSlotEntity.class))).thenReturn(testAppointmentSlot);

        // Act
        AppointmentSlotDTO result = assertDoesNotThrow(() -> appointmentSlotService.createAppointmentSlot(testAppointmentSlotRequest));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testAppointmentSlotDTO.getId(), result.getId());
            assertEquals(testAppointmentSlotDTO.getDoctor().getId(), result.getDoctor().getId());
            assertEquals(testAppointmentSlotDTO.getSpecialty().getId(), result.getSpecialty().getId());
            assertEquals(testAppointmentSlotDTO.getRoom().getId(), result.getRoom().getId());
            assertEquals(testAppointmentSlotDTO.getDate(), result.getDate());
            assertEquals(testAppointmentSlotDTO.getStartTime(), result.getStartTime());
            assertEquals(testAppointmentSlotDTO.getEndTime(), result.getEndTime());
        });

        verify(medicalProfileRepository).findById(1);
        verify(specialtyRepository).findById(1);
        verify(roomRepository).findById(1);
        verify(appointmentSlotRepository).existsRoomOverlappingSlot(any(), any(), any(), any());
        verify(appointmentSlotRepository).existsDoctorOverlappingSlot(any(), any(), any(), any());
        verify(appointmentSlotRepository).save(any(AppointmentSlotEntity.class));
    }

    @Test
    void C6_createAppointmentSlot_should_throw_exception_when_there_is_a_schedule_conflict() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(roomRepository.findById(1)).thenReturn(Optional.of(testRoom));
        when(appointmentSlotRepository.existsRoomOverlappingSlot(any(), any(), any(), any())).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> appointmentSlotService.createAppointmentSlot(testAppointmentSlotRequest));

        verify(medicalProfileRepository).findById(1);
        verify(specialtyRepository).findById(1);
        verify(roomRepository).findById(1);
        verify(appointmentSlotRepository).existsRoomOverlappingSlot(any(), any(), any(), any());
        verify(appointmentSlotRepository, never()).save(any(AppointmentSlotEntity.class));
    }

    @Test
    void C7_deleteAppointmentSlot_should_delete_appointment_slot_when_it_exists() {
        // Arrange
        when(appointmentSlotRepository.existsById(1)).thenReturn(true);
        doNothing().when(appointmentSlotRepository).deleteById(1);

        // Act
        assertDoesNotThrow(() -> appointmentSlotService.deleteAppointmentSlot(1));

        // Assert
        verify(appointmentSlotRepository).existsById(1);
        verify(appointmentSlotRepository).deleteById(1);
    }

    @Test
    void C8_deleteAppointmentSlot_should_throw_exception_when_appointment_slot_does_not_exist() {
        // Arrange
        when(appointmentSlotRepository.existsById(1)).thenReturn(false);

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> appointmentSlotService.deleteAppointmentSlot(1));

        // Assert
        assertAll(() -> {
            assertNotNull(exception);
            assertEquals("data_not_found.appointment_slot", exception.getCode());
        });

        verify(appointmentSlotRepository).existsById(1);
        verify(appointmentSlotRepository, never()).deleteById(anyInt());
    }

    @Test
    void C9_generateAppointmentSlotsByScheduleId_should_return_list_of_appointment_slots() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(7); // Asegurarse que sea lunes
        while (testDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            testDate = testDate.plusDays(1);
        }

        when(doctorScheduleRepository.findById(1)).thenReturn(Optional.of(testSchedule));
        when(appointmentSlotRepository.existsRoomOverlappingSlot(any(), any(), any(), any())).thenReturn(false);
        when(appointmentSlotRepository.existsDoctorOverlappingSlot(any(), any(), any(), any())).thenReturn(false);
        when(appointmentSlotRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LocalDate finalTestDate = testDate;
        List<AppointmentSlotDTO> result = assertDoesNotThrow(() -> appointmentSlotService.generateAppointmentSlotsByScheduleId(1, finalTestDate));

        // Assert
        LocalDate finalTestDate1 = testDate;
        assertAll(() -> {
            assertNotNull(result);
            assertFalse(result.isEmpty());
            // Verificamos que se hayan creado slots para cada franja de 30 minutos entre 9:00 y 14:00
            assertEquals((14 - 9) * 2, result.size()); // 5 horas * 2 slots por hora = 10 slots

            // Verificamos un slot aleatorio
            AppointmentSlotDTO firstSlot = result.getFirst();
            assertEquals(finalTestDate1, firstSlot.getDate());
            assertEquals(LocalTime.of(9, 0), firstSlot.getStartTime());
            assertEquals(LocalTime.of(9, 30), firstSlot.getEndTime());
            assertEquals(testDoctor.getId(), firstSlot.getDoctor().getId());
            assertEquals(testSpecialty.getId(), firstSlot.getSpecialty().getId());
            assertEquals(testRoom.getId(), firstSlot.getRoom().getId());
        });

        verify(doctorScheduleRepository).findById(1);
        verify(appointmentSlotRepository, times((14 - 9) * 2)).existsRoomOverlappingSlot(any(), any(), any(), any());
        verify(appointmentSlotRepository, times((14 - 9) * 2)).existsDoctorOverlappingSlot(any(), any(), any(), any());
        verify(appointmentSlotRepository).saveAll(any());
    }

    @Test
    void C10_getAvailableDatesByDoctorAndMedicalCenterAndSpecialty_should_return_list_of_available_slots() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(medicalCenterRepository.findById(1)).thenReturn(Optional.of(testMedicalCenter));
        when(appointmentSlotRepository.findAvailableDatesByDoctorAndMedicalCenterAndSpecialty(testMedicalCenter, testSpecialty, testDoctor)).thenReturn(testAppointmentSlots);

        // Act
        List<AppointmentSlotDTO> result = assertDoesNotThrow(() -> appointmentSlotService.getAvailableDatesByDoctorAndMedicalCenterAndSpecialty(1, 1, 1));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testAppointmentSlotDTO.getId(), result.getFirst().getId());
        });

        verify(medicalProfileRepository).findById(1);
        verify(specialtyRepository).findById(1);
        verify(medicalCenterRepository).findById(1);
        verify(appointmentSlotRepository).findAvailableDatesByDoctorAndMedicalCenterAndSpecialty(
                testMedicalCenter, testSpecialty, testDoctor);
    }
}