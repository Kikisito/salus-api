package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.SpecialtyDTO;
import com.kikisito.salus.api.dto.request.AddSpecialtyRequest;
import com.kikisito.salus.api.dto.response.SpecialtiesListResponse;
import com.kikisito.salus.api.entity.SpecialtyEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.SpecialtyRepository;
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
public class SpecialtyServiceTest {
    @MockitoBean
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SpecialtyService specialtyService;

    private SpecialtyEntity testSpecialty;
    private SpecialtyDTO testSpecialtyDTO;
    private Page<SpecialtyEntity> testPagedSpecialties;

    @BeforeEach
    void setUp() {
        testSpecialty = SpecialtyEntity.builder()
                .id(1)
                .name("Medicina General")
                .description("Un poco de todo")
                .build();

        testSpecialtyDTO = modelMapper.map(testSpecialty, SpecialtyDTO.class);
        testPagedSpecialties = new PageImpl<>(Collections.singletonList(testSpecialty));
    }

    @Test
    void C1_getAllSpecialties_should_return_one_specialty() {
        // Arrange
        when(specialtyRepository.findAll(any(PageRequest.class))).thenReturn(testPagedSpecialties);

        // Act
        SpecialtiesListResponse response = specialtyService.getAllSpecialties(Optional.of(0), Optional.of(10));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getCount());
            assertEquals(1, response.getSpecialties().size());
            assertEquals(testSpecialtyDTO.getId(), response.getSpecialties().get(0).getId());
            assertEquals(testSpecialtyDTO.getName(), response.getSpecialties().get(0).getName());
            assertEquals(testSpecialtyDTO.getDescription(), response.getSpecialties().get(0).getDescription());
        });

        verify(specialtyRepository).findAll(any(PageRequest.class));
    }

    @Test
    void C2_searchSpecialties_should_return_one_specialty_when_search_input_is_General() {
        // Arrange
        when(specialtyRepository.findByNameContainingIgnoreCase(eq("General"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(testSpecialty)));

        // Act
        SpecialtiesListResponse response = specialtyService.searchSpecialties("General", Optional.of(0), Optional.of(10));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getCount());
            assertEquals(1, response.getSpecialties().size());
            assertEquals(testSpecialtyDTO.getId(), response.getSpecialties().get(0).getId());
            assertEquals(testSpecialtyDTO.getName(), response.getSpecialties().get(0).getName());
        });

        verify(specialtyRepository).findByNameContainingIgnoreCase(anyString(), any(PageRequest.class));
    }

    @Test
    void C3_searchSpecialties_should_return_empty_list_when_search_input_is_Neurologia() {
        // Arrange
        when(specialtyRepository.findByNameContainingIgnoreCase(eq("Neurologia"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        SpecialtiesListResponse response = specialtyService.searchSpecialties("Neurologia", Optional.of(0), Optional.of(10));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(0, response.getCount());
            assertEquals(0, response.getSpecialties().size());
            assertArrayEquals(new SpecialtyDTO[0], response.getSpecialties().toArray());
        });

        verify(specialtyRepository).findByNameContainingIgnoreCase(anyString(), any(PageRequest.class));
    }

    @Test
    void C4_getSpecialty_should_return_specialty_when_id_is_1() {
        // Arrange
        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));

        // Act
        SpecialtyDTO response = specialtyService.getSpecialty(1);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testSpecialtyDTO.getId(), response.getId());
            assertEquals(testSpecialtyDTO.getName(), response.getName());
            assertEquals(testSpecialtyDTO.getDescription(), response.getDescription());
        });

        verify(specialtyRepository).findById(anyInt());
    }

    @Test
    void C5_getSpecialty_should_throw_exception_when_specialty_not_found() {
        // Arrange
        when(specialtyRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> specialtyService.getSpecialty(2));

        // Assert
        assertEquals("data_not_found.specialty", exception.getCode());

        verify(specialtyRepository).findById(anyInt());
    }

    @Test
    void C6_addSpecialty_should_return_added_specialty() {
        // Arrange
        AddSpecialtyRequest specialtyRequest = AddSpecialtyRequest.builder()
                .name("Cardiología")
                .description("La especialidad del corazón")
                .build();

        when(specialtyRepository.save(any(SpecialtyEntity.class))).thenReturn(testSpecialty);

        // Act
        SpecialtyDTO response = specialtyService.addSpecialty(specialtyRequest);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testSpecialtyDTO.getId(), response.getId());
            assertEquals(testSpecialtyDTO.getName(), response.getName());
            assertEquals(testSpecialtyDTO.getDescription(), response.getDescription());
        });

        verify(specialtyRepository).save(any(SpecialtyEntity.class));
    }

    @Test
    void C7_updateSpecialty_should_return_updated_specialty() {
        // Arrange
        AddSpecialtyRequest updateRequest = AddSpecialtyRequest.builder()
                .name("Neurología")
                .description("La especialidad de los trastornos del sistema nervioso")
                .build();

        SpecialtyEntity updatedSpecialty = SpecialtyEntity.builder()
                .id(1)
                .name("Neurología")
                .description("La especialidad de los trastornos del sistema nervioso")
                .build();

        when(specialtyRepository.findById(1)).thenReturn(Optional.of(testSpecialty));
        when(specialtyRepository.save(any(SpecialtyEntity.class))).thenReturn(updatedSpecialty);

        // Act
        SpecialtyDTO response = specialtyService.updateSpecialty(1, updateRequest);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getId());
            assertEquals(updatedSpecialty.getName(), response.getName());
            assertEquals(updatedSpecialty.getDescription(), response.getDescription());
        });

        verify(specialtyRepository).findById(anyInt());
        verify(specialtyRepository).save(any(SpecialtyEntity.class));
    }

    @Test
    void C8_updateSpecialty_should_throw_exception_when_specialty_not_found() {
        // Arrange
        AddSpecialtyRequest updateRequest = AddSpecialtyRequest.builder()
                .name("Neurología")
                .description("La especialidad de los trastornos del sistema nervioso")
                .build();

        when(specialtyRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> specialtyService.updateSpecialty(2, updateRequest));

        // Assert
        assertEquals("data_not_found.specialty", exception.getCode());

        verify(specialtyRepository).findById(anyInt());
        verify(specialtyRepository, never()).save(any(SpecialtyEntity.class));
    }
}