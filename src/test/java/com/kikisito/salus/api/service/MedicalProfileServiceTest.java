package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.MedicalProfileDTO;
import com.kikisito.salus.api.dto.SpecialtyDTO;
import com.kikisito.salus.api.dto.request.AddDoctorSpecialtyRequest;
import com.kikisito.salus.api.dto.request.DoctorLicenseRequest;
import com.kikisito.salus.api.dto.response.DoctorsListResponse;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.SpecialtyEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import com.kikisito.salus.api.type.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MedicalProfileServiceTest {

    @MockitoBean
    private MedicalProfileRepository medicalProfileRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SpecialtyRepository specialtyRepository;

    @MockitoBean
    private MedicalCenterRepository medicalCenterRepository;

    @MockitoBean
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MedicalProfileService medicalProfileService;

    private UserEntity testUser;
    private MedicalProfileEntity testMedicalProfile;
    private MedicalProfileDTO testMedicalProfileDTO;
    private SpecialtyEntity testSpecialty;
    private SpecialtyDTO testSpecialtyDTO;
    private Page<MedicalProfileEntity> testPagedMedicalProfiles;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1)
                .nombre("Mariano")
                .apellidos("García")
                .email("mariano@salus.com")
                .nif("12345678Z")
                .rolesList(new ArrayList<>(List.of(RoleType.USER)))
                .build();

        testSpecialty = SpecialtyEntity.builder()
                .id(1)
                .name("Medicina General")
                .description("Especialidad de medicina general")
                .build();

        testSpecialtyDTO = modelMapper.map(testSpecialty, SpecialtyDTO.class);

        testMedicalProfile = MedicalProfileEntity.builder()
                .id(1)
                .user(testUser)
                .license("COLEG-TEST-1")
                .specialties(new ArrayList<>(Collections.singletonList(testSpecialty)))
                .build();

        testMedicalProfileDTO = modelMapper.map(testMedicalProfile, MedicalProfileDTO.class);

        testPagedMedicalProfiles = new PageImpl<>(Collections.singletonList(testMedicalProfile));
    }

    @Test
    void C1_getMedicalProfiles_should_return_one_medical_profile() {
        // Arrange
        when(medicalProfileRepository.findAll(any(PageRequest.class))).thenReturn(testPagedMedicalProfiles);
        when(medicalProfileRepository.count()).thenReturn(1L);

        // Act
        DoctorsListResponse response = medicalProfileService.getMedicalProfiles(0, 10);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getCount());
            assertEquals(1, response.getDoctors().size());
            assertEquals(testMedicalProfileDTO.getId(), response.getDoctors().get(0).getId());
            assertEquals(testMedicalProfileDTO.getLicense(), response.getDoctors().get(0).getLicense());
        });

        verify(medicalProfileRepository).findAll(any(PageRequest.class));
        verify(medicalProfileRepository).count();
    }

    @Test
    void C2_searchMedicalProfiles_should_return_one_medical_profile() {
        // Arrange
        when(medicalProfileRepository.search(anyString(), any(PageRequest.class))).thenReturn(testPagedMedicalProfiles);
        when(medicalProfileRepository.searchCount(anyString())).thenReturn(1);

        // Act
        DoctorsListResponse response = medicalProfileService.searchMedicalProfiles("Mariano", 0, 10);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getCount());
            assertEquals(1, response.getDoctors().size());
            assertEquals(testMedicalProfileDTO.getId(), response.getDoctors().get(0).getId());
            assertEquals(testMedicalProfileDTO.getLicense(), response.getDoctors().get(0).getLicense());
        });

        verify(medicalProfileRepository).search(anyString(), any(PageRequest.class));
        verify(medicalProfileRepository).searchCount(anyString());
    }

    @Test
    void C3_getMedicalProfile_should_return_medical_profile_when_id_is_1() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testMedicalProfile));

        // Act
        MedicalProfileDTO response = medicalProfileService.getMedicalProfile(1);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testMedicalProfileDTO.getId(), response.getId());
            assertEquals(testMedicalProfileDTO.getLicense(), response.getLicense());
        });

        verify(medicalProfileRepository).findById(anyInt());
    }

    @Test
    void C4_getMedicalProfile_should_throw_exception_when_getting_a_medical_profile_that_does_not_exist() {
        // Arrange
        when(medicalProfileRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> medicalProfileService.getMedicalProfile(999));

        // Assert
        assertEquals("data_not_found.doctor", exception.getCode());
        verify(medicalProfileRepository).findById(anyInt());
    }

    @Test
    void C5_createMedicalProfileFromUserEntity_should_return_the_created_medical_profile() {
        // Arrange
        DoctorLicenseRequest doctorLicenseRequest = DoctorLicenseRequest.builder()
                .userId(1)
                .license("COLEG-TEST-1")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(medicalProfileRepository.existsMedicoEntitiesByUser(any(UserEntity.class))).thenReturn(false);
        when(medicalProfileRepository.existsMedicoEntitiesByLicense(anyString())).thenReturn(false);
        when(medicalProfileRepository.save(any(MedicalProfileEntity.class))).thenReturn(testMedicalProfile);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        MedicalProfileDTO response = medicalProfileService.createMedicalProfileFromUserEntity(doctorLicenseRequest);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testMedicalProfileDTO.getId(), response.getId());
            assertEquals(testMedicalProfileDTO.getLicense(), response.getLicense());
        });

        verify(userRepository).findById(anyInt());
        verify(medicalProfileRepository).existsMedicoEntitiesByUser(any(UserEntity.class));
        verify(medicalProfileRepository).existsMedicoEntitiesByLicense(anyString());
        verify(medicalProfileRepository).save(any(MedicalProfileEntity.class));
    }

    @Test
    void C6_addSpecialtyToMedicalProfile_should_return_the_updated_medical_profile() {
        // Arrange
        AddDoctorSpecialtyRequest request = AddDoctorSpecialtyRequest.builder()
                .specialtyId(1)
                .build();

        // Creamos un perfil médico sin especialidades para esta prueba
        MedicalProfileEntity profile = MedicalProfileEntity.builder()
                .id(1)
                .user(testUser)
                .license("COLEG-TEST-1")
                .specialties(new ArrayList<>())
                .build();

        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(profile));
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(medicalProfileRepository.save(any(MedicalProfileEntity.class))).thenReturn(testMedicalProfile);

        // Act
        MedicalProfileDTO response = medicalProfileService.addSpecialtyToMedicalProfile(1, request);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testMedicalProfileDTO.getId(), response.getId());
            assertEquals(testMedicalProfileDTO.getLicense(), response.getLicense());
            assertEquals(1, response.getSpecialties().size());
            assertEquals(testSpecialtyDTO.getId(), response.getSpecialties().get(0).getId());
        });

        verify(medicalProfileRepository).findById(anyInt());
        verify(specialtyRepository).findById(anyInt());
        verify(medicalProfileRepository).save(any(MedicalProfileEntity.class));
    }

    @Test
    void C7_deleteMedicalProfile_should_return_true_when_medical_profile_is_deleted() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testMedicalProfile));
        when(appointmentRepository.existsBySlot_Doctor(any(MedicalProfileEntity.class))).thenReturn(false);
        doNothing().when(medicalProfileRepository).delete(any(MedicalProfileEntity.class));

        // Act
        boolean result = medicalProfileService.deleteMedicalProfile(1);

        // Assert
        assertTrue(result);

        verify(medicalProfileRepository).findById(anyInt());
        verify(appointmentRepository).existsBySlot_Doctor(any(MedicalProfileEntity.class));
        verify(medicalProfileRepository).delete(any(MedicalProfileEntity.class));
    }

    @Test
    void C8_deleteMedicalProfile_should_throw_exception_when_medical_profile_does_not_exist() {
        // Arrange
        when(medicalProfileRepository.findById(2)).thenReturn(Optional.empty());
        when(appointmentRepository.existsBySlot_Doctor(any(MedicalProfileEntity.class))).thenReturn(false);
        doNothing().when(medicalProfileRepository).delete(any(MedicalProfileEntity.class));

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> medicalProfileService.deleteMedicalProfile(2));

        // Assert
        assertEquals("data_not_found.doctor", exception.getCode());
        verify(medicalProfileRepository).findById(anyInt());
        verify(medicalProfileRepository, never()).delete(any(MedicalProfileEntity.class));
    }
}