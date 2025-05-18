package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.ReportDTO;
import com.kikisito.salus.api.dto.request.ReportRequest;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.type.ReportType;
import com.kikisito.salus.api.type.RoleType;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
public class ReportServiceTest {

    @MockitoBean
    private ReportRepository reportRepository;

    @MockitoBean
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private MedicalProfileRepository medicalProfileRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private MedicalCenterRepository medicalCenterRepository;

    @MockitoBean
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ReportService reportService;

    private UserEntity testPatient;
    private MedicalProfileEntity testDoctor;
    private SpecialtyEntity testSpecialty;
    private AppointmentEntity testAppointment;
    private ReportEntity testReport;
    private ReportDTO testReportDTO;
    private ReportRequest testReportRequest;

    @BeforeEach
    void setUp() {
        // Paciente de prueba
        testPatient = UserEntity.builder()
                .id(1)
                .nombre("Paciente")
                .apellidos("Test")
                .email("paciente@salus.com")
                .nif("12345678A")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
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
                        .nombre("Doctor")
                        .apellidos("Test")
                        .email("doctor@salus.com")
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

        // Informe de prueba
        testReport = ReportEntity.builder()
                .id(1)
                .patient(testPatient)
                .doctor(testDoctor)
                .specialty(testSpecialty)
                .appointment(testAppointment)
                .type(ReportType.GENERAL)
                .description("Informe de prueba")
                .diagnosis("Diagnóstico de prueba")
                .treatment("Tratamiento de prueba")
                .observations("Observaciones de prueba")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(testDoctor.getUser())
                .lastModifiedBy(testDoctor.getUser())
                .build();

        testReportDTO = modelMapper.map(testReport, ReportDTO.class);

        // ReportRequest de prueba
        testReportRequest = ReportRequest.builder()
                .doctor(1)
                .patient(1)
                .specialty(1)
                .appointment(1)
                .type(ReportType.GENERAL)
                .description("Informe de prueba")
                .diagnosis("Diagnóstico de prueba")
                .treatment("Tratamiento de prueba")
                .observations("Observaciones de prueba")
                .build();
    }

    @Test
    void C1_getUserReports_should_return_one_report() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(reportRepository.findByPatient(testPatient)).thenReturn(Collections.singletonList(testReport));

        // Act
        List<ReportDTO> reports = reportService.getUserReports(1);

        // Assert
        assertAll(() -> {
            assertNotNull(reports);
            assertEquals(1, reports.size());
            assertEquals(testReportDTO.getId(), reports.getFirst().getId());
            assertEquals(testReportDTO.getDescription(), reports.getFirst().getDescription());
            assertEquals(testReportDTO.getDiagnosis(), reports.getFirst().getDiagnosis());
        });

        verify(userRepository).findById(1);
        verify(reportRepository).findByPatient(testPatient);
    }

    @Test
    void C2_getAppointmentReports_should_return_one_report() {
        // Arrange
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));
        when(reportRepository.findByAppointment(testAppointment)).thenReturn(Collections.singletonList(testReport));

        // Act
        List<ReportDTO> reports = reportService.getAppointmentReports(1);

        // Assert
        assertAll(() -> {
            assertNotNull(reports);
            assertEquals(1, reports.size());
            assertEquals(testReportDTO.getId(), reports.getFirst().getId());
            assertEquals(testReportDTO.getDescription(), reports.getFirst().getDescription());
            assertEquals(testReportDTO.getDiagnosis(), reports.getFirst().getDiagnosis());
        });

        verify(appointmentRepository).findById(1);
        verify(reportRepository).findByAppointment(testAppointment);
    }

    @Test
    void C3_getPatientReportsWithDoctorOrItsSpecialties_should_return_one_report() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(reportRepository.findByPatientWithDoctorOrItsSpecialties(testPatient, testDoctor, testDoctor.getSpecialties()))
                .thenReturn(Collections.singletonList(testReport));

        // Act
        List<ReportDTO> reports = reportService.getPatientReportsWithDoctorOrItsSpecialties(1, 1);

        // Assert
        assertAll(() -> {
            assertNotNull(reports);
            assertEquals(1, reports.size());
            assertEquals(testReportDTO.getId(), reports.getFirst().getId());
            assertEquals(testReportDTO.getDescription(), reports.getFirst().getDescription());
            assertEquals(testReportDTO.getDiagnosis(), reports.getFirst().getDiagnosis());
        });

        verify(userRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(reportRepository).findByPatientWithDoctorOrItsSpecialties(testPatient, testDoctor, testDoctor.getSpecialties());
    }

    @Test
    void C4_addReport_should_return_the_created_report() {
        // Arrange
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(testAppointment));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(reportRepository.save(any(ReportEntity.class))).thenReturn(testReport);

        // Act
        ReportDTO result = reportService.addReport(testReportRequest);

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testReportDTO.getId(), result.getId());
            assertEquals(testReportDTO.getDescription(), result.getDescription());
            assertEquals(testReportDTO.getDiagnosis(), result.getDiagnosis());
            assertEquals(testReportDTO.getTreatment(), result.getTreatment());
            assertEquals(testReportDTO.getObservations(), result.getObservations());
        });

        verify(appointmentRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(userRepository).findById(1);
        verify(specialtyRepository).findById(1);
        verify(reportRepository).save(any(ReportEntity.class));
    }

    @Test
    void C5_getReport_should_return_report_when_requested_report_exists() {
        // Arrange
        when(reportRepository.findById(1)).thenReturn(Optional.of(testReport));

        // Act
        ReportDTO result = reportService.getReport(1);

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testReportDTO.getId(), result.getId());
            assertEquals(testReportDTO.getDescription(), result.getDescription());
            assertEquals(testReportDTO.getDiagnosis(), result.getDiagnosis());
        });

        verify(reportRepository).findById(1);
    }

    @Test
    void C6_getReport_should_throw_exception_when_getting_a_report_that_does_not_exist() {
        // Arrange
        when(reportRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> reportService.getReport(2));

        // Assert
        assertEquals("data_not_found.report", exception.getCode());
        verify(reportRepository).findById(2);
    }

    @Test
    void C7_canProfessionalAccessReport_should_return_true_when_report_is_associated_to_doctor() {
        // Arrange
        when(reportRepository.findById(1)).thenReturn(Optional.of(testReport));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));

        // Act
        boolean result = reportService.canProfessionalAccessReport(1, 1);

        // Assert
        assertTrue(result);
        verify(reportRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
    }

    @Test
    void C8_canProfessionalAccessReport_should_return_false_when_report_is_not_associated_to_doctor() {
        // Arrange
        MedicalProfileEntity medicalProfileEntity = MedicalProfileEntity.builder()
                .id(3)
                .specialties(new ArrayList<>())
                .build();

        when(reportRepository.findById(1)).thenReturn(Optional.of(testReport));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(medicalProfileEntity));

        // Act
        boolean result = reportService.canProfessionalAccessReport(1, 1);

        // Assert
        assertFalse(result);
        verify(reportRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
    }

    @Test
    void C9_updateReport_should_return_the_updated_report() {
        // Arrange
        ReportRequest updateRequest = ReportRequest.builder()
                .description("Informe de consulta")
                .diagnosis("Alergia al polen")
                .treatment("Antihistamínicos")
                .observations("Evitar el contacto con alérgenos")
                .build();

        ReportEntity updatedReport = ReportEntity.builder()
                .id(1)
                .description("Informe de consulta")
                .diagnosis("Alergia al polen")
                .treatment("Antihistamínicos")
                .observations("Evitar el contacto con alérgenos")
                .build();

        when(reportRepository.findById(1)).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(ReportEntity.class))).thenReturn(updatedReport);

        // Act
        ReportDTO result = reportService.updateReport(1, updateRequest);

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Informe de consulta", result.getDescription());
            assertEquals("Alergia al polen", result.getDiagnosis());
            assertEquals("Antihistamínicos", result.getTreatment());
            assertEquals("Evitar el contacto con alérgenos", result.getObservations());
        });

        verify(reportRepository).findById(1);
        verify(reportRepository).save(any(ReportEntity.class));
    }

    @Test
    void C10_deleteReport_should_not_throw_exception_when_deleting_a_report() {
        // Arrange
        when(reportRepository.findById(1)).thenReturn(Optional.of(testReport));
        doNothing().when(reportRepository).delete(testReport);

        // Act
        assertDoesNotThrow(() -> reportService.deleteReport(1));

        // Assert
        verify(reportRepository).findById(1);
        verify(reportRepository).delete(testReport);
    }

    @Test
    void C11_getReportPatient_should_return_the_patient_of_the_report() {
        // Arrange
        when(reportRepository.findById(1)).thenReturn(Optional.of(testReport));

        // Act
        UserEntity result = reportService.getReportPatient(1);

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testPatient.getId(), result.getId());
            assertEquals(testPatient.getNombre(), result.getNombre());
            assertEquals(testPatient.getApellidos(), result.getApellidos());
            assertEquals(testPatient.getNif(), result.getNif());
        });

        verify(reportRepository).findById(1);
    }
}