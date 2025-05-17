package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.MedicalCenterDTO;
import com.kikisito.salus.api.dto.request.NewMedicalCenterRequest;
import com.kikisito.salus.api.dto.response.MedicalCentersListResponse;
import com.kikisito.salus.api.entity.MedicalCenterEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.MedicalCenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MedicalCenterServiceTest {
    @MockitoBean
    private MedicalCenterRepository medicalCenterRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MedicalCenterService medicalCenterService;

    private MedicalCenterEntity testMedicalCenter;
    private MedicalCenterDTO testMedicalCenterDTO;
    private Page<MedicalCenterEntity> testPagedMedicalCenters;

    @BeforeEach
    void setUp() {
        // Entidad inicial de las pruebas
        testMedicalCenter = MedicalCenterEntity.builder()
                .id(1)
                .name("Hospital de Pruebas")
                .email("test@salus.com")
                .phone("632498123")
                .addressLine1("Calle de Unidad de las Pruebas 3")
                .zipCode("03001")
                .country("España")
                .province("Alicante")
                .municipality("Alicante")
                .locality("Alicante")
                .build();

        testMedicalCenterDTO = modelMapper.map(testMedicalCenter, MedicalCenterDTO.class);

        // Asignamos la entidad inicial al paginado de centros médicos
        testPagedMedicalCenters = new PageImpl<>(Collections.singletonList(testMedicalCenter));
    }

    @Test
    void C1_getMedicalCenters_should_return_one_medical_center() {
        // Arrange
        when(medicalCenterRepository.findAll(any(PageRequest.class))).thenReturn(testPagedMedicalCenters);

        // Act
        MedicalCentersListResponse response = medicalCenterService.getMedicalCenters(Optional.of(0), Optional.of(10));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getCount());
            assertEquals(1, response.getMedicalCenters().size());
            assertEquals(testMedicalCenterDTO.getId(), response.getMedicalCenters().get(0).getId());
            assertEquals(testMedicalCenterDTO.getName(), response.getMedicalCenters().get(0).getName());
        });

        verify(medicalCenterRepository).findAll(any(PageRequest.class));
    }

    @Test
    void C2_searchMedicalCenters_should_return_one_medical_center_when_search_input_is_Pruebas(){
        // Arrange
        when(medicalCenterRepository.findByNameContainingIgnoreCase(eq("Pruebas"), any(PageRequest.class))).thenReturn(testPagedMedicalCenters);

        // Act
        MedicalCentersListResponse response = medicalCenterService.searchMedicalCenters("Pruebas", Optional.of(0), Optional.of(10));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getCount());
            assertEquals(1, response.getMedicalCenters().size());
            assertEquals(testMedicalCenterDTO.getId(), response.getMedicalCenters().get(0).getId());
            assertEquals(testMedicalCenterDTO.getName(), response.getMedicalCenters().get(0).getName());
        });

        verify(medicalCenterRepository).findByNameContainingIgnoreCase(anyString(), any(PageRequest.class));
    }

    @Test
    void C3_searchMedicalCenters_should_return_empty_list_when_search_input_is_Probando() {
        // Arrange
        when(medicalCenterRepository.findByNameContainingIgnoreCase(eq("Probando"), any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        MedicalCentersListResponse response = medicalCenterService.searchMedicalCenters("Probando", Optional.of(0), Optional.of(10));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(0, response.getCount());
            assertEquals(0, response.getMedicalCenters().size());
            assertArrayEquals(new MedicalCenterDTO[0], response.getMedicalCenters().toArray());
        });

        verify(medicalCenterRepository).findByNameContainingIgnoreCase(anyString(), any(PageRequest.class));
    }

    @Test
    void C4_getMedicalCentersByAvailableSpecialtys_should_return_one_medical_center_when_specialty_id_is_1() {
        // Arrange
        when(medicalCenterRepository.findByAvailableSpecialty(eq(1), any(PageRequest.class))).thenReturn(testPagedMedicalCenters);

        // Act
        MedicalCentersListResponse response = medicalCenterService.getMedicalCentersByAvailableSpecialty(1, Optional.of(0), Optional.of(10));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getCount());
            assertEquals(1, response.getMedicalCenters().size());
            assertEquals(testMedicalCenterDTO.getId(), response.getMedicalCenters().get(0).getId());
            assertEquals(testMedicalCenterDTO.getName(), response.getMedicalCenters().get(0).getName());
        });

        verify(medicalCenterRepository).findByAvailableSpecialty(anyInt(), any(PageRequest.class));
    }

    @Test
    void C5_getMedicalCentersByAvailableSpecialtys_should_return_empty_list_when_specialty_id_is_2() {
        // Arrange
        when(medicalCenterRepository.findByAvailableSpecialty(eq(2), any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        MedicalCentersListResponse response = medicalCenterService.getMedicalCentersByAvailableSpecialty(2, Optional.of(0), Optional.of(10));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(0, response.getCount());
            assertEquals(0, response.getMedicalCenters().size());
            assertArrayEquals(new MedicalCenterDTO[0], response.getMedicalCenters().toArray());
        });

        verify(medicalCenterRepository).findByAvailableSpecialty(anyInt(), any(PageRequest.class));
    }

    @Test
    void C6_getMedicalCenterById_should_return_medical_center_when_id_is_1() {
        // Arrange
        when(medicalCenterRepository.findById(1)).thenReturn(Optional.of(testMedicalCenter));

        // Act
        MedicalCenterDTO response = medicalCenterService.getMedicalCenterById(1);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testMedicalCenterDTO.getId(), response.getId());
            assertEquals(testMedicalCenterDTO.getName(), response.getName());
        });

        verify(medicalCenterRepository).findById(anyInt());
    }

    @Test
    void C7_getMedicalCenterById_should_throw_exception_when_getting_a_medical_center_that_does_not_exist() {
        // Arrange
        when(medicalCenterRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> medicalCenterService.getMedicalCenterById(2));

        // Assert
        assertEquals("data_not_found.medical_center", exception.getCode());
        verify(medicalCenterRepository).findById(anyInt());
    }

    @Test
    void C8_addMedicalCenter_should_return_the_added_medical_center() {
        // Arrange
        NewMedicalCenterRequest newMedicalCenter = NewMedicalCenterRequest.builder()
                .name(testMedicalCenter.getName())
                .email(testMedicalCenter.getEmail())
                .phone(testMedicalCenter.getPhone())
                .addressLine1(testMedicalCenter.getAddressLine1())
                .addressLine2(testMedicalCenter.getAddressLine2())
                .zipCode(testMedicalCenter.getZipCode())
                .country(testMedicalCenter.getCountry())
                .province(testMedicalCenter.getProvince())
                .municipality(testMedicalCenter.getMunicipality())
                .locality(testMedicalCenter.getLocality())
                .build();

        when(medicalCenterRepository.save(any(MedicalCenterEntity.class))).thenReturn(testMedicalCenter);

        // Act
        MedicalCenterDTO response = assertDoesNotThrow(() -> medicalCenterService.addMedicalCenter(newMedicalCenter));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testMedicalCenterDTO.getId(), response.getId());
            assertEquals(testMedicalCenterDTO.getName(), response.getName());
            assertEquals(testMedicalCenterDTO.getEmail(), response.getEmail());
            assertEquals(testMedicalCenterDTO.getPhone(), response.getPhone());
            assertEquals(testMedicalCenterDTO.getAddressLine1(), response.getAddressLine1());
            assertEquals(testMedicalCenterDTO.getAddressLine2(), response.getAddressLine2());
            assertEquals(testMedicalCenterDTO.getZipCode(), response.getZipCode());
            assertEquals(testMedicalCenterDTO.getCountry(), response.getCountry());
            assertEquals(testMedicalCenterDTO.getProvince(), response.getProvince());
            assertEquals(testMedicalCenterDTO.getMunicipality(), response.getMunicipality());
            assertEquals(testMedicalCenterDTO.getLocality(), response.getLocality());
        });
        verify(medicalCenterRepository).save(any(MedicalCenterEntity.class));
    }

    @Test
    void C9_updateMedicalCenter_should_return_the_updated_medical_center() {
        // Arrange
        NewMedicalCenterRequest updatedMedicalCenter = NewMedicalCenterRequest.builder()
                .name("Hospital de Pruebas Actualizado")
                .email(testMedicalCenter.getEmail())
                .phone(testMedicalCenter.getPhone())
                .addressLine1(testMedicalCenter.getAddressLine1())
                .addressLine2(testMedicalCenter.getAddressLine2())
                .zipCode(testMedicalCenter.getZipCode())
                .country(testMedicalCenter.getCountry())
                .province(testMedicalCenter.getProvince())
                .municipality(testMedicalCenter.getMunicipality())
                .locality(testMedicalCenter.getLocality())
                .build();

        when(medicalCenterRepository.findById(1)).thenReturn(Optional.of(testMedicalCenter));
        when(medicalCenterRepository.save(any(MedicalCenterEntity.class))).thenReturn(modelMapper.map(updatedMedicalCenter, MedicalCenterEntity.class));

        // Act
        MedicalCenterDTO response = assertDoesNotThrow(() -> medicalCenterService.updateMedicalCenter(1, updatedMedicalCenter));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(updatedMedicalCenter.getName(), response.getName());
            assertEquals(updatedMedicalCenter.getEmail(), response.getEmail());
            assertEquals(updatedMedicalCenter.getPhone(), response.getPhone());
            assertEquals(updatedMedicalCenter.getAddressLine1(), response.getAddressLine1());
            assertEquals(updatedMedicalCenter.getAddressLine2(), response.getAddressLine2());
            assertEquals(updatedMedicalCenter.getZipCode(), response.getZipCode());
            assertEquals(updatedMedicalCenter.getCountry(), response.getCountry());
            assertEquals(updatedMedicalCenter.getProvince(), response.getProvince());
            assertEquals(updatedMedicalCenter.getMunicipality(), response.getMunicipality());
            assertEquals(updatedMedicalCenter.getLocality(), response.getLocality());
        });

        verify(medicalCenterRepository).findById(anyInt());
        verify(medicalCenterRepository).save(any(MedicalCenterEntity.class));
    }

    @Test
    void C10_updateMedicalCenter_should_throw_exception_when_updating_a_medical_center_that_does_not_exist() {
        // Arrange
        NewMedicalCenterRequest updatedMedicalCenter = NewMedicalCenterRequest.builder()
                .name("Hospital de Pruebas Actualizado")
                .email(testMedicalCenter.getEmail())
                .phone(testMedicalCenter.getPhone())
                .addressLine1(testMedicalCenter.getAddressLine1())
                .addressLine2(testMedicalCenter.getAddressLine2())
                .zipCode(testMedicalCenter.getZipCode())
                .country(testMedicalCenter.getCountry())
                .province(testMedicalCenter.getProvince())
                .municipality(testMedicalCenter.getMunicipality())
                .locality(testMedicalCenter.getLocality())
                .build();

        when(medicalCenterRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> medicalCenterService.updateMedicalCenter(2, updatedMedicalCenter));

        // Assert
        assertEquals("data_not_found.medical_center", exception.getCode());
        verify(medicalCenterRepository).findById(anyInt());
    }
}