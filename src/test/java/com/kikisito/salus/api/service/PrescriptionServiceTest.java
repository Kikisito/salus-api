package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.PrescriptionDTO;
import com.kikisito.salus.api.dto.request.MedicationRequest;
import com.kikisito.salus.api.dto.request.PrescriptionRequest;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import com.kikisito.salus.api.type.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PrescriptionServiceTest {

    @MockitoBean
    private PrescriptionRepository prescriptionRepository;

    @MockitoBean
    private MedicationRepository medicationRepository;

    @MockitoBean
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private MedicalProfileRepository medicalProfileRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SpecialtyRepository specialtyRepository;

    @MockitoBean
    private MedicalCenterRepository medicalCenterRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PrescriptionService prescriptionService;

    private UserEntity testPatient;
    private MedicalProfileEntity testDoctor;
    private SpecialtyEntity testSpecialty;
    private AppointmentEntity testAppointment;
    private PrescriptionEntity testPrescription;
    private MedicationEntity testMedication;
    private PrescriptionDTO testPrescriptionDTO;
    private PrescriptionRequest testPrescriptionRequest;

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
        testSpecialty = SpecialtyEntity.builder()
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
                        .nif("24958901Z")
                        .rolesList(new ArrayList<>(List.of(RoleType.USER, RoleType.PROFESSIONAL)))
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

        // AppointmentSlot de prueba
        AppointmentSlotEntity testSlot = AppointmentSlotEntity.builder()
                .id(1)
                .doctor(testDoctor)
                .date(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .room(testRoom)
                .specialty(testSpecialty)
                .build();

        // Cita de prueba
        testAppointment = AppointmentEntity.builder()
                .id(1)
                .patient(testPatient)
                .slot(testSlot)
                .build();

        // Medicación de prueba
        testMedication = MedicationEntity.builder()
                .id(1)
                .name("Paracetamol")
                .dosage("1 comprimido")
                .frequency(BigDecimal.valueOf(8))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .instructions("Tomar después de las comidas")
                .build();

        // Receta de prueba
        testPrescription = PrescriptionEntity.builder()
                .id(1)
                .patient(testPatient)
                .doctor(testDoctor)
                .specialty(testSpecialty)
                .appointment(testAppointment)
                .medications(new ArrayList<>(Collections.singletonList(testMedication)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Asignamos la receta a la medicación
        testMedication.setPrescription(testPrescription);

        testPrescriptionDTO = modelMapper.map(testPrescription, PrescriptionDTO.class);

        // PrescriptionRequest de prueba
        MedicationRequest medicationRequest = MedicationRequest.builder()
                .name("Paracetamol")
                .dosage("1 comprimido")
                .frequency(BigDecimal.valueOf(8))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .instructions("Tomar después de las comidas")
                .build();

        testPrescriptionRequest = PrescriptionRequest.builder()
                .doctor(1)
                .patient(1)
                .specialty(1)
                .appointment(1)
                .medications(new ArrayList<>(Collections.singletonList(medicationRequest)))
                .build();
    }

    @Test
    void C1_getAppointmentPrescriptions_should_return_one_prescription() {
        // Arrange
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));
        when(prescriptionRepository.findAllByAppointment(testAppointment)).thenReturn(Collections.singletonList(testPrescription));

        // Act
        List<PrescriptionDTO> prescriptions = assertDoesNotThrow(() -> prescriptionService.getAppointmentPrescriptions(1));

        // Assert
        assertAll(() -> {
            assertNotNull(prescriptions);
            assertEquals(1, prescriptions.size());
            assertEquals(testPrescriptionDTO.getId(), prescriptions.getFirst().getId());
            assertEquals(testPrescriptionDTO.getDoctor().getId(), prescriptions.getFirst().getDoctor().getId());
            assertEquals(testPrescriptionDTO.getPatient().getId(), prescriptions.getFirst().getPatient().getId());
            assertEquals(testPrescriptionDTO.getMedications().size(), prescriptions.getFirst().getMedications().size());
        });

        verify(appointmentRepository).findById(1);
        verify(prescriptionRepository).findAllByAppointment(testAppointment);
    }

    @Test
    void C2_getDoctorPrescriptions_should_return_one_prescription() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(prescriptionRepository.findAllByDoctor(testDoctor)).thenReturn(Collections.singletonList(testPrescription));

        // Act
        List<PrescriptionDTO> prescriptions = assertDoesNotThrow(() -> prescriptionService.getDoctorPrescriptions(1));

        // Assert
        assertAll(() -> {
            assertNotNull(prescriptions);
            assertEquals(1, prescriptions.size());
            assertEquals(testPrescriptionDTO.getId(), prescriptions.getFirst().getId());
            assertEquals(testPrescriptionDTO.getDoctor().getId(), prescriptions.getFirst().getDoctor().getId());
            assertEquals(testPrescriptionDTO.getPatient().getId(), prescriptions.getFirst().getPatient().getId());
        });

        verify(medicalProfileRepository).findById(1);
        verify(prescriptionRepository).findAllByDoctor(testDoctor);
    }

    @Test
    void C3_getPatientPrescriptions_should_return_one_prescription() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(prescriptionRepository.findAllByPatient(testPatient)).thenReturn(Collections.singletonList(testPrescription));

        // Act
        List<PrescriptionDTO> prescriptions = assertDoesNotThrow(() -> prescriptionService.getPatientPrescriptions(1));

        // Assert
        assertAll(() -> {
            assertNotNull(prescriptions);
            assertEquals(1, prescriptions.size());
            assertEquals(testPrescriptionDTO.getId(), prescriptions.getFirst().getId());
            assertEquals(testPrescriptionDTO.getDoctor().getId(), prescriptions.getFirst().getDoctor().getId());
            assertEquals(testPrescriptionDTO.getPatient().getId(), prescriptions.getFirst().getPatient().getId());
        });

        verify(userRepository).findById(1);
        verify(prescriptionRepository).findAllByPatient(testPatient);
    }

    @Test
    void C4_addPrescription_should_return_the_created_prescription() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));
        when(prescriptionRepository.save(any(PrescriptionEntity.class))).thenReturn(testPrescription);
        when(medicationRepository.save(any(MedicationEntity.class))).thenReturn(testMedication);

        // Act
        PrescriptionDTO result = assertDoesNotThrow(() -> prescriptionService.addPrescription(testPrescriptionRequest));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testPrescriptionDTO.getId(), result.getId());
            assertEquals(testPrescriptionDTO.getDoctor().getId(), result.getDoctor().getId());
            assertEquals(testPrescriptionDTO.getPatient().getId(), result.getPatient().getId());
            assertEquals(testPrescriptionDTO.getSpecialty().getId(), result.getSpecialty().getId());
            assertEquals(testPrescriptionDTO.getAppointmentId(), result.getAppointmentId());
            assertEquals(testPrescriptionDTO.getMedications().size(), result.getMedications().size());
        });

        verify(medicalProfileRepository).findById(1);
        verify(userRepository).findById(1);
        verify(specialtyRepository).findById(1);
        verify(appointmentRepository).findById(1);
        verify(prescriptionRepository, times(2)).save(any(PrescriptionEntity.class));
        verify(medicationRepository).save(any(MedicationEntity.class));
    }

    @Test
    void C5_updatePrescription_should_return_the_updated_prescription() {
        // Arrange
        MedicationRequest updatedMedicationRequest = MedicationRequest.builder()
                .id(1)
                .name("Ibuprofeno")
                .dosage("1 comprimido")
                .frequency(BigDecimal.valueOf(12))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .instructions("Tomar con las comidas")
                .build();

        PrescriptionRequest updateRequest = PrescriptionRequest.builder()
                .medications(new ArrayList<>(Collections.singletonList(updatedMedicationRequest)))
                .build();

        MedicationEntity updatedMedication = MedicationEntity.builder()
                .id(1)
                .name("Ibuprofeno")
                .dosage("1 comprimido")
                .frequency(BigDecimal.valueOf(12))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .instructions("Tomar con las comidas")
                .build();

        PrescriptionEntity updatedPrescription = PrescriptionEntity.builder()
                .id(1)
                .patient(testPatient)
                .doctor(testDoctor)
                .specialty(testSpecialty)
                .appointment(testAppointment)
                .medications(new ArrayList<>(Collections.singletonList(updatedMedication)))
                .build();

        when(prescriptionRepository.findById(1)).thenReturn(Optional.of(testPrescription));
        when(prescriptionRepository.save(any(PrescriptionEntity.class))).thenReturn(updatedPrescription);

        // Act
        PrescriptionDTO result = assertDoesNotThrow(() -> prescriptionService.updatePrescription(1, updateRequest));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals(1, result.getMedications().size());
            assertEquals("Ibuprofeno", result.getMedications().getFirst().getName());
            assertEquals(BigDecimal.valueOf(12), result.getMedications().getFirst().getFrequency());
        });

        verify(prescriptionRepository).findById(1);
        verify(prescriptionRepository, times(2)).save(any(PrescriptionEntity.class));
    }

    @Test
    void C6_canProfessionalAccessPrescription_should_return_true_when_prescription_is_associated_to_doctor() {
        // Arrange
        when(prescriptionRepository.findById(1)).thenReturn(Optional.of(testPrescription));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));

        // Act
        boolean result = assertDoesNotThrow(() -> prescriptionService.canProfessionalAccessPrescription(1, 1));

        // Assert
        assertTrue(result);

        verify(prescriptionRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
    }

    @Test
    void C7_canProfessionalAccessPrescription_should_return_false_when_prescription_is_not_associated_to_doctor() {
        // Arrange
        MedicalProfileEntity otherDoctor = MedicalProfileEntity.builder()
                .id(2)
                .user(UserEntity.builder().id(3).build())
                .license("COLEG-TEST-2")
                .specialties(new ArrayList<>())
                .build();

        when(prescriptionRepository.findById(1)).thenReturn(Optional.of(testPrescription));
        when(medicalProfileRepository.findById(2)).thenReturn(Optional.of(otherDoctor));

        // Act
        boolean result = assertDoesNotThrow(() -> prescriptionService.canProfessionalAccessPrescription(1, 2));

        // Assert
        assertFalse(result);

        verify(prescriptionRepository).findById(1);
        verify(medicalProfileRepository).findById(2);
    }

    @Test
    void C8_addPrescription_should_throw_exception_when_doctor_not_found() {
        // Arrange
        PrescriptionRequest requestWithInvalidDoctor = PrescriptionRequest.builder()
                .doctor(3)
                .patient(1)
                .specialty(1)
                .build();

        when(medicalProfileRepository.findById(3)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> prescriptionService.addPrescription(requestWithInvalidDoctor));

        // Assert
        assertEquals("data_not_found.doctor", exception.getCode());
        verify(medicalProfileRepository).findById(3);
    }

    @Test
    void C9_addPrescription_should_throw_exception_when_patient_not_found() {
        // Arrange
        PrescriptionRequest requestWithInvalidDoctor = PrescriptionRequest.builder()
                .doctor(1)
                .patient(3)
                .specialty(1)
                .build();

        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(userRepository.findById(3)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> prescriptionService.addPrescription(requestWithInvalidDoctor));

        // Assert
        assertEquals("data_not_found.user", exception.getCode());
        verify(medicalProfileRepository).findById(1);
        verify(userRepository).findById(3);
    }

    @Test
    void C10_addPrescription_should_throw_exception_when_specialty_not_found() {
        // Arrange
        PrescriptionRequest requestWithInvalidSpecialty = PrescriptionRequest.builder()
                .doctor(1)
                .patient(1)
                .specialty(3)
                .build();

        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(specialtyRepository.findById(3)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> prescriptionService.addPrescription(requestWithInvalidSpecialty));

        // Assert
        assertEquals("data_not_found.specialty", exception.getCode());
        verify(medicalProfileRepository).findById(1);
        verify(userRepository).findById(1);
        verify(specialtyRepository).findById(3);
    }
}