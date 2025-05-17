package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.RoomDTO;
import com.kikisito.salus.api.dto.request.RoomRequest;
import com.kikisito.salus.api.dto.response.RoomsListResponse;
import com.kikisito.salus.api.entity.MedicalCenterEntity;
import com.kikisito.salus.api.entity.RoomEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.MedicalCenterRepository;
import com.kikisito.salus.api.repository.RoomRepository;
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
public class RoomServiceTest {
    @MockitoBean
    private RoomRepository roomRepository;

    @MockitoBean
    private MedicalCenterRepository medicalCenterRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RoomService roomService;

    private MedicalCenterEntity testMedicalCenter;

    private RoomEntity testRoom;
    private RoomDTO testRoomDTO;
    private Page<RoomEntity> testPagedRooms;

    @BeforeEach
    void setUp() {
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

        testRoom = RoomEntity.builder()
                .id(1)
                .name("Consulta 1")
                .medicalCenter(testMedicalCenter)
                .build();
        testRoomDTO = modelMapper.map(testRoom, RoomDTO.class);
        testPagedRooms = new PageImpl<>(Collections.singletonList(testRoom));
    }

    @Test
    void C1_getRoom_should_return_room_when_id_is_1() {
        // Arrange
        when(roomRepository.findById(1)).thenReturn(Optional.of(testRoom));

        // Act
        RoomDTO response = roomService.getRoom(1);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testRoomDTO.getId(), response.getId());
            assertEquals(testRoomDTO.getName(), response.getName());
            assertEquals(testRoomDTO.getMedicalCenter().getId(), response.getMedicalCenter().getId());
        });

        verify(roomRepository).findById(anyInt());
    }

    @Test
    void C2_getRoom_should_throw_exception_when_room_not_found() {
        // Arrange
        when(roomRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> roomService.getRoom(2));

        // Assert
        assertEquals("data_not_found.room", exception.getCode());

        verify(roomRepository).findById(anyInt());
    }

    @Test
    void C3_addRoom_should_return_added_room() {
        // Arrange
        RoomRequest roomRequest = RoomRequest.builder()
                .name("Consulta 1")
                .medicalCenter(1)
                .build();

        when(medicalCenterRepository.findById(1)).thenReturn(Optional.of(testMedicalCenter));
        when(roomRepository.save(any(RoomEntity.class))).thenReturn(testRoom);

        // Act
        RoomDTO response = roomService.addRoom(roomRequest);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testRoomDTO.getId(), response.getId());
            assertEquals(testRoomDTO.getName(), response.getName());
            assertEquals(testRoomDTO.getMedicalCenter().getId(), response.getMedicalCenter().getId());
        });

        verify(medicalCenterRepository).findById(anyInt());
        verify(roomRepository).save(any(RoomEntity.class));
    }

    @Test
    void C4_addRoom_should_throw_exception_when_medical_center_not_found() {
        // Arrange
        RoomRequest roomRequest = RoomRequest.builder()
                .name("Consulta 1")
                .medicalCenter(2)
                .build();

        when(medicalCenterRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> roomService.addRoom(roomRequest));

        // Assert
        assertEquals("data_not_found.medical_center", exception.getCode());

        verify(medicalCenterRepository).findById(anyInt());
        // Verificamos que la función de guardar NO se ha llamado
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    void C5_getRooms_should_return_rooms_list() {
        // Arrange
        when(roomRepository.findAll(any(PageRequest.class))).thenReturn(testPagedRooms);

        // Act
        RoomsListResponse response = roomService.getRooms(Optional.of(0), Optional.of(10));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getCount());
            assertEquals(testRoomDTO.getId(), response.getRooms().get(0).getId());
            assertEquals(testRoomDTO.getName(), response.getRooms().get(0).getName());
            assertEquals(testRoomDTO.getMedicalCenter().getId(), response.getRooms().get(0).getMedicalCenter().getId());
        });

        verify(roomRepository).findAll(any(PageRequest.class));
    }

    @Test
    void C7_updateRoom_should_return_updated_room() {
        // Arrange
        RoomRequest updateRequest = RoomRequest.builder()
                .name("Consulta 1 Actualizada")
                .medicalCenter(1)
                .build();

        RoomEntity updatedRoom = RoomEntity.builder()
                .id(1)
                .name("Consulta 1 Actualizada")
                .medicalCenter(testMedicalCenter)
                .build();

        when(roomRepository.findById(1)).thenReturn(Optional.of(testRoom));
        when(medicalCenterRepository.findById(1)).thenReturn(Optional.of(testMedicalCenter));
        when(roomRepository.save(any(RoomEntity.class))).thenReturn(updatedRoom);

        // Act
        RoomDTO response = roomService.updateRoom(1, updateRequest);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getId());
            assertEquals("Consulta 1 Actualizada", response.getName());
            assertEquals(testMedicalCenter.getId(), response.getMedicalCenter().getId());
        });

        verify(roomRepository).findById(anyInt());
        verify(medicalCenterRepository).findById(anyInt());
        verify(roomRepository).save(any(RoomEntity.class));
    }

    @Test
    void C8_updateRoom_should_throw_exception_when_room_not_found() {
        // Arrange
        RoomRequest updateRequest = RoomRequest.builder()
                .name("Consulta 1 Actualizada")
                .medicalCenter(1)
                .build();

        when(medicalCenterRepository.findById(1)).thenReturn(Optional.of(testMedicalCenter));
        when(roomRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> roomService.updateRoom(2, updateRequest));

        // Assert
        assertEquals("data_not_found.room", exception.getCode());

        verify(medicalCenterRepository).findById(anyInt());
        verify(roomRepository).findById(anyInt());
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    void C9_updateRoom_should_throw_exception_when_medical_center_not_found() {
        // Arrange
        RoomRequest updateRequest = RoomRequest.builder()
                .name("Consulta 1 Actualizada")
                .medicalCenter(2)
                .build();

        when(roomRepository.findById(1)).thenReturn(Optional.of(testRoom));
        when(medicalCenterRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> roomService.updateRoom(1, updateRequest));

        // Assert
        assertEquals("data_not_found.medical_center", exception.getCode());
        verify(medicalCenterRepository).findById(anyInt());
        verify(roomRepository, never()).findById(anyInt());
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }
}