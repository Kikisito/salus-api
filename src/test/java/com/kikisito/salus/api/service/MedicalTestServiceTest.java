package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.MedicalTestDTO;
import com.kikisito.salus.api.dto.request.MedicalTestRequest;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.type.RoleType;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

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
public class MedicalTestServiceTest {

    @MockitoBean
    private MedicalTestRepository medicalTestRepository;

    @MockitoBean
    private AttachmentService attachmentService;

    @MockitoBean
    private MedicalProfileRepository medicalProfileRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SpecialtyRepository specialtyRepository;

    @MockitoBean
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private AttachmentRepository attachmentRepository;

    @MockitoBean
    private MedicalCenterRepository medicalCenterRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MedicalTestService medicalTestService;

    private UserEntity testPatient;
    private MedicalProfileEntity testDoctor;
    private SpecialtyEntity testSpecialty;
    private AppointmentEntity testAppointment;
    private MedicalTestEntity testMedicalTest;
    private MedicalTestDTO testMedicalTestDTO;
    private MedicalTestRequest testMedicalTestRequest;
    private AttachmentEntity testAttachment;

    @BeforeEach
    void setUp() {
        // Paciente de prueba
        testPatient = UserEntity.builder()
                .id(1)
                .nombre("José")
                .apellidos("López")
                .email("joselopez@salus.com")
                .nif("12345678A")
                .fechaNacimiento(LocalDate.of(1984, 7, 4))
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
                        .nombre("Juan")
                        .apellidos("Martínez")
                        .email("juanmart@salus.com")
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

        // AppointmentSlot de prueba
        AppointmentSlotEntity testSlot = AppointmentSlotEntity.builder()
                .id(1)
                .doctor(testDoctor)
                .date(LocalDate.now())
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

        // Prueba médica de prueba
        testMedicalTest = MedicalTestEntity.builder()
                .id(1)
                .name("Análisis de sangre")
                .description("Análisis de sangre completo")
                .patient(testPatient)
                .doctor(testDoctor)
                .specialty(testSpecialty)
                .appointment(testAppointment)
                .requestedAt(LocalDate.now())
                .scheduledAt(LocalDate.now().plusDays(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Archivo de prueba
        testAttachment = AttachmentEntity.builder()
                .id(1)
                .name("resultado.pdf")
                .contentType("application/pdf")
                .size(1024L)
                .filePath("/uploads/medical-tests/1/resultado.pdf")
                .medicalTest(testMedicalTest)
                .build();

        testMedicalTestDTO = modelMapper.map(testMedicalTest, MedicalTestDTO.class);

        // MedicalTestRequest de prueba
        testMedicalTestRequest = MedicalTestRequest.builder()
                .doctor(1)
                .patient(1)
                .specialty(1)
                .appointment(1)
                .name("Análisis de sangre")
                .description("Análisis de sangre completo")
                .requestedAt(LocalDate.now())
                .scheduledAt(LocalDate.now().plusDays(2))
                .build();
    }

    @Test
    void C1_getPatientMedicalTests_should_return_one_medical_test() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(medicalTestRepository.findByPatient(testPatient)).thenReturn(Collections.singletonList(testMedicalTest));

        // Act
        List<MedicalTestDTO> result = assertDoesNotThrow(() -> medicalTestService.getPatientMedicalTests(testPatient.getId()));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testMedicalTestDTO.getId(), result.getFirst().getId());
            assertEquals(testMedicalTestDTO.getName(), result.getFirst().getName());
            assertEquals(testMedicalTestDTO.getDescription(), result.getFirst().getDescription());
        });

        verify(userRepository).findById(1);
        verify(medicalTestRepository).findByPatient(testPatient);
    }

    @Test
    void C2_getPatientMedicalTestsWithDoctorOrItsSpecialties_should_return_one_medical_test() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(medicalTestRepository.findByPatientWithDoctorOrItsSpecialties(testPatient, testDoctor, testDoctor.getSpecialties()))
                .thenReturn(Collections.singletonList(testMedicalTest));

        // Act
        List<MedicalTestDTO> result = assertDoesNotThrow(() -> medicalTestService.getPatientMedicalTestsWithDoctorOrItsSpecialties(1, 1));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testMedicalTestDTO.getId(), result.getFirst().getId());
            assertEquals(testMedicalTestDTO.getName(), result.getFirst().getName());
            assertEquals(testMedicalTestDTO.getDescription(), result.getFirst().getDescription());
        });

        verify(userRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(medicalTestRepository).findByPatientWithDoctorOrItsSpecialties(testPatient, testDoctor, testDoctor.getSpecialties());
    }

    @Test
    void C3_addMedicalTest_should_return_the_created_medical_test() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test file content".getBytes());
        List<MultipartFile> files = Collections.singletonList(file);

        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));
        when(medicalTestRepository.save(any(MedicalTestEntity.class))).thenReturn(testMedicalTest);
        when(attachmentService.saveAttachment(any(MultipartFile.class))).thenReturn(testAttachment);

        // Act
        MedicalTestDTO result = assertDoesNotThrow(() -> medicalTestService.addMedicalTest(testMedicalTestRequest, files));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testMedicalTestDTO.getId(), result.getId());
            assertEquals(testMedicalTestDTO.getName(), result.getName());
            assertEquals(testMedicalTestDTO.getDescription(), result.getDescription());
            assertEquals(testMedicalTestDTO.getDoctor().getId(), result.getDoctor().getId());
            assertEquals(testMedicalTestDTO.getPatient().getId(), result.getPatient().getId());
            assertEquals(testMedicalTestDTO.getSpecialty().getId(), result.getSpecialty().getId());
        });

        verify(medicalProfileRepository).findById(1);
        verify(userRepository).findById(1);
        verify(specialtyRepository).findById(1);
        verify(appointmentRepository).findById(1);
        verify(medicalTestRepository).save(any(MedicalTestEntity.class));
        verify(attachmentService).saveAttachment(any(MultipartFile.class));
        verify(attachmentRepository).save(any(AttachmentEntity.class));
    }

    @Test
    void C4_deleteMedicalTest_should_delete_medical_test_and_its_attachments() {
        // Arrange
        when(medicalTestRepository.findById(1)).thenReturn(Optional.of(testMedicalTest));
        when(attachmentRepository.findByMedicalTest(testMedicalTest)).thenReturn(Collections.singletonList(testAttachment));
        doNothing().when(attachmentService).deleteAttachment(anyInt());
        doNothing().when(medicalTestRepository).delete(any(MedicalTestEntity.class));

        // Act
        assertDoesNotThrow(() -> medicalTestService.deleteMedicalTest(1));

        // Assert
        verify(medicalTestRepository).findById(1);
        verify(attachmentRepository).findByMedicalTest(testMedicalTest);
        verify(attachmentService).deleteAttachment(1);
        verify(medicalTestRepository).delete(testMedicalTest);
    }

    @Test
    void C5_deleteMedicalTest_should_throw_exception_when_medical_test_not_found() {
        // Arrange
        when(medicalTestRepository.findById(3)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> medicalTestService.deleteMedicalTest(3));

        // Assert
        assertEquals("data_not_found.medical_test", exception.getCode());
        verify(medicalTestRepository).findById(3);
        verify(attachmentRepository, never()).findByMedicalTest(any(MedicalTestEntity.class));
        verify(attachmentService, never()).deleteAttachment(anyInt());
        verify(medicalTestRepository, never()).delete(any(MedicalTestEntity.class));
    }

    @Test
    void C6_isDoctorResponsibleOfMedicalTest_should_return_true_when_medical_test_is_associated_to_doctor() {
        // Arrange
        when(medicalTestRepository.findById(1)).thenReturn(Optional.of(testMedicalTest));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));

        // Act
        boolean result = assertDoesNotThrow(() -> medicalTestService.isDoctorResponsibleOfMedicalTest(1, 1));

        // Assert
        assertTrue(result);

        verify(medicalTestRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
    }

    @Test
    void C7_isDoctorResponsibleOfMedicalTest_should_return_false_when_medical_test_is_not_associated_to_doctor() {
        // Arrange
        MedicalProfileEntity otherDoctor = MedicalProfileEntity.builder()
                .id(2)
                .user(UserEntity.builder().id(3).build())
                .specialties(new ArrayList<>())
                .build();

        when(medicalTestRepository.findById(1)).thenReturn(Optional.of(testMedicalTest));
        when(medicalProfileRepository.findById(2)).thenReturn(Optional.of(otherDoctor));

        // Act
        boolean result = assertDoesNotThrow(() -> medicalTestService.isDoctorResponsibleOfMedicalTest(1, 2));

        // Assert
        assertFalse(result);

        verify(medicalTestRepository).findById(1);
        verify(medicalProfileRepository).findById(2);
    }

    @Test
    void C8_getReportPatient_should_return_the_patient_of_the_medical_test() {
        // Arrange
        when(medicalTestRepository.findById(1)).thenReturn(Optional.of(testMedicalTest));

        // Act
        UserEntity result = medicalTestService.getReportPatient(1);

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testPatient.getId(), result.getId());
            assertEquals(testPatient.getNombre(), result.getNombre());
            assertEquals(testPatient.getApellidos(), result.getApellidos());
            assertEquals(testPatient.getNif(), result.getNif());
        });

        verify(medicalTestRepository).findById(1);
    }

    @Test
    void C9_getPatientMedicalTests_should_throw_exception_when_user_not_found() {
        // Arrange
        when(userRepository.findById(3)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> medicalTestService.getPatientMedicalTests(3));

        // Assert
        assertEquals("data_not_found.user", exception.getCode());

        verify(userRepository).findById(3);
        verify(medicalTestRepository, never()).findByPatient(any(UserEntity.class));
    }

    @Test
    void C10_addMedicalTest_should_throw_exception_when_doctor_not_found() {
        // Arrange
        testMedicalTestRequest.setDoctor(999);
        when(medicalProfileRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> medicalTestService.addMedicalTest(testMedicalTestRequest, null));

        // Assert
        assertEquals("data_not_found.doctor", exception.getCode());

        verify(medicalProfileRepository).findById(999);
        verify(medicalTestRepository, never()).save(any(MedicalTestEntity.class));
    }
}