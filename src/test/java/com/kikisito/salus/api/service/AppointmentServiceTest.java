package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.AppointmentDTO;
import com.kikisito.salus.api.dto.ReducedAppointmentDTO;
import com.kikisito.salus.api.dto.request.AppointmentRequest;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import com.kikisito.salus.api.type.AppointmentStatusType;
import com.kikisito.salus.api.type.AppointmentType;
import com.kikisito.salus.api.type.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AppointmentServiceTest {

    @MockitoBean
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private AppointmentSlotRepository appointmentSlotRepository;

    @MockitoBean
    private MedicalProfileRepository medicalProfileRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ReportRepository reportRepository;

    @MockitoBean
    private PrescriptionRepository prescriptionRepository;

    @MockitoBean
    private MedicalTestRepository medicalTestRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AppointmentService appointmentService;

    private UserEntity testPatient;
    private MedicalProfileEntity testDoctor;
    private AppointmentSlotEntity testAppointmentSlot;
    private AppointmentEntity testAppointment;
    private AppointmentDTO testAppointmentDTO;
    private ReducedAppointmentDTO testReducedAppointmentDTO;
    private AppointmentRequest testAppointmentRequest;
    private List<AppointmentEntity> testAppointments;
    private ReportEntity testReport;
    private PrescriptionEntity testPrescription;
    private MedicalTestEntity testMedicalTest;

    @BeforeEach
    void setUp() {
        // Paciente de prueba
        testPatient = UserEntity.builder()
                .id(1)
                .nombre("Juan")
                .apellidos("García")
                .email("juang@salus.com")
                .nif("12345678A")
                .fechaNacimiento(LocalDate.of(1992, 1, 7))
                .rolesList(new ArrayList<>(List.of(RoleType.USER)))
                .build();

        // Especialidad de prueba
        SpecialtyEntity testSpecialty = SpecialtyEntity.builder()
                .id(1)
                .name("Medicina General")
                .description("Especialidad de medicina general")
                .build();

        // Doctor de prueba
        testDoctor = MedicalProfileEntity.builder()
                .id(1)
                .user(UserEntity.builder()
                        .id(2)
                        .nombre("María")
                        .apellidos("Guerrero")
                        .email("mariag@salus.com")
                        .nif("87654321B")
                        .rolesList(new ArrayList<>(List.of(RoleType.PROFESSIONAL)))
                        .build())
                .license("COLEG-TEST-1")
                .specialties(new ArrayList<>(Collections.singletonList(testSpecialty)))
                .build();

        // Centro médico de prueba
        MedicalCenterEntity testMedicalCenter = MedicalCenterEntity.builder()
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
        RoomEntity testRoom = RoomEntity.builder()
                .id(1)
                .name("Consulta 1")
                .medicalCenter(testMedicalCenter)
                .build();

        // Hueco de cita de prueba
        testAppointmentSlot = AppointmentSlotEntity.builder()
                .id(1)
                .doctor(testDoctor)
                .specialty(testSpecialty)
                .room(testRoom)
                .date(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .build();

        // Cita de prueba
        testAppointment = AppointmentEntity.builder()
                .id(1)
                .patient(testPatient)
                .slot(testAppointmentSlot)
                .type(AppointmentType.IN_PERSON)
                .reason("Consulta de rutina")
                .status(AppointmentStatusType.PENDING)
                .doctorObservations("Paciente tiene dolor de garganta")
                .build();

        // Relación slot-cita
        testAppointmentSlot.setAppointment(testAppointment);

        testAppointmentDTO = modelMapper.map(testAppointment, AppointmentDTO.class);
        testReducedAppointmentDTO = modelMapper.map(testAppointment, ReducedAppointmentDTO.class);

        // AppointmentRequest de prueba
        testAppointmentRequest = AppointmentRequest.builder()
                .patient(1)
                .appointmentSlot(1)
                .type(AppointmentType.IN_PERSON)
                .reason("Consulta de rutina")
                .build();

        // Listado de citas
        testAppointments = new ArrayList<>(Collections.singletonList(testAppointment));

        // Otras entidades de prueba
        testReport = ReportEntity.builder()
                .id(1)
                .appointment(testAppointment)
                .doctor(testDoctor)
                .patient(testPatient)
                .specialty(testSpecialty)
                .build();

        testPrescription = PrescriptionEntity.builder()
                .id(1)
                .appointment(testAppointment)
                .doctor(testDoctor)
                .patient(testPatient)
                .specialty(testSpecialty)
                .build();

        testMedicalTest = MedicalTestEntity.builder()
                .id(1)
                .appointment(testAppointment)
                .doctor(testDoctor)
                .patient(testPatient)
                .specialty(testSpecialty)
                .build();
    }

    @Test
    void C1_getAllDoctorAppointmentsByDate_should_return_list_of_appointments() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(7);
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findBySlot_DoctorAndSlot_Date(testDoctor, testDate)).thenReturn(testAppointments);

        // Act
        List<AppointmentDTO> result = assertDoesNotThrow(() -> appointmentService.getAllDoctorAppointmentsByDate(1, testDate));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testAppointmentDTO.getId(), result.getFirst().getId());
            assertEquals(testAppointmentDTO.getSlot().getId(), result.getFirst().getSlot().getId());
            assertEquals(testAppointmentDTO.getPatient().getId(), result.getFirst().getPatient().getId());
        });

        verify(medicalProfileRepository).findById(1);
        verify(appointmentRepository).findBySlot_DoctorAndSlot_Date(testDoctor, testDate);
    }

    @Test
    void C2_getAppointmentById_should_return_appointment_when_it_exists() {
        // Arrange
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));

        // Act
        AppointmentDTO result = assertDoesNotThrow(() -> appointmentService.getAppointmentById(1));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testAppointmentDTO.getId(), result.getId());
            assertEquals(testAppointmentDTO.getSlot().getId(), result.getSlot().getId());
            assertEquals(testAppointmentDTO.getPatient().getId(), result.getPatient().getId());
            assertEquals(testAppointmentDTO.getType(), result.getType());
            assertEquals(testAppointmentDTO.getReason(), result.getReason());
            assertEquals(testAppointmentDTO.getStatus(), result.getStatus());
            assertEquals(testAppointmentDTO.getDoctorObservations(), result.getDoctorObservations());
        });

        verify(appointmentRepository).findById(1);
    }

    @Test
    void C3_getAppointmentById_should_throw_exception_when_appointment_does_not_exist() {
        // Arrange
        when(appointmentRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> appointmentService.getAppointmentById(1));

        // Assert
        assertAll(() -> {
            assertNotNull(exception);
            assertEquals("data_not_found.appointment", exception.getCode());
        });

        verify(appointmentRepository).findById(1);
    }

    @Test
    void C4_getUserAppointments_should_return_list_of_user_appointments() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findByPatient(testPatient)).thenReturn(testAppointments);

        // Act
        List<AppointmentDTO> result = assertDoesNotThrow(() -> appointmentService.getUserAppointments(1));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testAppointmentDTO.getId(), result.getFirst().getId());
        });

        verify(userRepository).findById(1);
        verify(appointmentRepository).findByPatient(testPatient);
    }

    @Test
    void C5_getPatientAppointmentsWithDoctorOrItsSpecialties_should_return_list_of_appointments() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findByPatientWithDoctorOrItsSpecialties(testPatient, testDoctor, testDoctor.getSpecialties())).thenReturn(testAppointments);

        // Act
        List<AppointmentDTO> result = assertDoesNotThrow(() -> appointmentService.getPatientAppointmentsWithDoctorOrItsSpecialties(1, 1));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testAppointmentDTO.getId(), result.getFirst().getId());
        });

        verify(userRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(appointmentRepository).findByPatientWithDoctorOrItsSpecialties(testPatient, testDoctor, testDoctor.getSpecialties());
    }

    @Test
    void C6_getUserUpcomingAppointmentsReduced_should_return_list_of_reduced_appointments() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findUpcomingAppointmentsByPatient(testPatient)).thenReturn(testAppointments);

        // Act
        List<ReducedAppointmentDTO> result = assertDoesNotThrow(() -> appointmentService.getUserUpcomingAppointmentsReduced(1));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testReducedAppointmentDTO.getId(), result.getFirst().getId());
        });

        verify(userRepository).findById(1);
        verify(appointmentRepository).findUpcomingAppointmentsByPatient(testPatient);
    }

    @Test
    void C7_getUserPastAppointmentsReduced_should_return_list_of_reduced_appointments() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findPastAppointmentsByPatient(testPatient)).thenReturn(testAppointments);

        // Act
        List<ReducedAppointmentDTO> result = assertDoesNotThrow(() -> appointmentService.getUserPastAppointmentsReduced(1));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testReducedAppointmentDTO.getId(), result.getFirst().getId());
        });

        verify(userRepository).findById(1);
        verify(appointmentRepository).findPastAppointmentsByPatient(testPatient);
    }

    @Test
    void C8_createAppointment_should_return_created_appointment() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(appointmentSlotRepository.findById(1)).thenReturn(Optional.of(testAppointmentSlot));
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenReturn(testAppointment);

        // Ensure slot doesn't have appointment
        testAppointmentSlot.setAppointment(null);

        // Act
        AppointmentDTO result = assertDoesNotThrow(() -> appointmentService.createAppointment(testAppointmentRequest));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testAppointmentDTO.getId(), result.getId());
            assertEquals(testAppointmentDTO.getSlot().getId(), result.getSlot().getId());
            assertEquals(testAppointmentDTO.getPatient().getId(), result.getPatient().getId());
            assertEquals(testAppointmentDTO.getType(), result.getType());
            assertEquals(testAppointmentDTO.getReason(), result.getReason());
        });

        verify(userRepository).findById(1);
        verify(appointmentSlotRepository).findById(1);
        verify(appointmentRepository).save(any(AppointmentEntity.class));
    }

    @Test
    void C9_createAppointment_should_throw_exception_when_slot_is_already_taken() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(appointmentSlotRepository.findById(1)).thenReturn(Optional.of(testAppointmentSlot));

        // Act
        ConflictException exception = assertThrows(ConflictException.class, () -> appointmentService.createAppointment(testAppointmentRequest));

        // Assert
        assertEquals("conflict.appointment_slot_is_already_taken", exception.getCode());

        verify(userRepository).findById(1);
        verify(appointmentSlotRepository).findById(1);
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void C10_updateAppointmentDoctorObservations_should_return_updated_appointment() {
        // Arrange
        String newObservations = "El paciente presenta fiebre alta";
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));

        // Al guardar, actualizamos las observaciones
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenAnswer(invocation -> {
            AppointmentEntity appointment = invocation.getArgument(0);
            appointment.setDoctorObservations(newObservations);
            return appointment;
        });

        // Act
        AppointmentDTO result = assertDoesNotThrow(() -> appointmentService.updateAppointmentDoctorObservations(1, newObservations));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testAppointmentDTO.getId(), result.getId());
            assertEquals(newObservations, result.getDoctorObservations());
        });

        verify(appointmentRepository).findById(1);
        verify(appointmentRepository).save(any(AppointmentEntity.class));
    }

    @Test
    void C11_updateAppointmentStatus_should_return_updated_appointment() {
        // Arrange
        AppointmentStatusType newStatus = AppointmentStatusType.COMPLETED;
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenAnswer(invocation -> {
            AppointmentEntity appointment = invocation.getArgument(0);
            appointment.setStatus(newStatus);
            return appointment;
        });

        // Act
        AppointmentDTO result = assertDoesNotThrow(() -> appointmentService.updateAppointmentStatus(1, newStatus));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testAppointmentDTO.getId(), result.getId());
            assertEquals(newStatus, result.getStatus());
        });

        verify(appointmentRepository).findById(1);
        verify(appointmentRepository).save(any(AppointmentEntity.class));
    }

    @Test
    void C12_deleteAppointment_should_delete_appointment_and_its_relationships() {
        // Arrange
        UserEntity admin = UserEntity.builder()
                .id(3)
                .rolesList(new ArrayList<>(List.of(RoleType.ADMIN)))
                .build();

        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));
        testAppointment.setReports(new ArrayList<>(List.of(testReport)));
        testAppointment.setPrescriptions(new ArrayList<>(List.of(testPrescription)));
        testAppointment.setMedicalTests(new ArrayList<>(List.of(testMedicalTest)));

        doNothing().when(appointmentRepository).delete(any(AppointmentEntity.class));

        // Act
        assertDoesNotThrow(() -> appointmentService.deleteAppointment(1, admin));

        // Assert
        verify(appointmentRepository).findById(1);
        verify(reportRepository).save(any(ReportEntity.class));
        verify(prescriptionRepository).save(any(PrescriptionEntity.class));
        verify(medicalTestRepository).save(any(MedicalTestEntity.class));
        verify(appointmentSlotRepository).save(any(AppointmentSlotEntity.class));
        verify(appointmentRepository).delete(testAppointment);
    }

    @Test
    void C13_canUserDeleteAppointment_should_return_true_when_user_can_delete_appointment() {
        // Arrange
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));
        testAppointmentSlot.setDate(LocalDate.now().plusDays(2));

        // Act
        boolean result = assertDoesNotThrow(() -> appointmentService.canUserDeleteAppointment(1, testPatient));

        // Assert
        assertTrue(result);
        verify(appointmentRepository).findById(1);
    }

    @Test
    void C14_canUserDeleteAppointment_should_return_false_when_appointment_is_too_soon() {
        // Arrange
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));
        testAppointmentSlot.setDate(LocalDate.now().plusDays(1));

        // Act
        boolean result = assertDoesNotThrow(() -> appointmentService.canUserDeleteAppointment(1, testPatient));

        // Assert
        assertFalse(result);
        verify(appointmentRepository).findById(1);
    }

    @Test
    void C15_canUserAccessAppointment_should_return_true_when_appointment_belongs_to_user() {
        // Arrange
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));

        // Act
        boolean result = assertDoesNotThrow(() -> appointmentService.canUserAccessAppointment(1, testPatient));

        // Assert
        assertTrue(result);
        verify(appointmentRepository).findById(1);
    }

    @Test
    void C16_canProfessionalAccessAppointment_should_return_true_when_doctor_is_responsible() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));

        // Act
        boolean result = assertDoesNotThrow(() -> appointmentService.canProfessionalAccessAppointment(1, 1));

        // Assert
        assertTrue(result);
        verify(medicalProfileRepository).findById(1);
        verify(appointmentRepository).findById(1);
    }

    @Test
    void C17_patientHasAtLeastOneAppointmentWithDoctor_should_return_true_when_relationship_exists() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsBySlot_DoctorAndPatient(testDoctor, testPatient)).thenReturn(true);

        // Act
        boolean result = assertDoesNotThrow(() -> appointmentService.patientHasAtLeastOneAppointmentWithDoctor(1, 1));

        // Assert
        assertTrue(result);
        verify(userRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(appointmentRepository).existsBySlot_DoctorAndPatient(testDoctor, testPatient);
    }

    @Test
    void C18_countAppointmentsByPatient_should_return_correct_count() {
        // Arrange
        when(appointmentRepository.countByPatient(testPatient)).thenReturn(1);

        // Act
        int result = assertDoesNotThrow(() -> appointmentService.countAppointmentsByPatient(testPatient));

        // Assert
        assertEquals(1, result);
        verify(appointmentRepository).countByPatient(testPatient);
    }
}