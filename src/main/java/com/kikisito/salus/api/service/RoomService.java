package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.RoomDTO;
import com.kikisito.salus.api.dto.request.RoomRequest;
import com.kikisito.salus.api.dto.response.RoomsListResponse;
import com.kikisito.salus.api.entity.MedicalCenterEntity;
import com.kikisito.salus.api.entity.RoomEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.MedicalCenterRepository;
import com.kikisito.salus.api.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    @Autowired
    private final MedicalCenterRepository medicalCenterRepository;

    @Autowired
    private final RoomRepository roomRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int MAX_ROWS_PER_PAGE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public RoomsListResponse getRooms(Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        // Usamos los métodos Math.max y Math.min para asegurarnos de que
        // los valores de page y limit estén dentro de los límites permitidos
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);
        
        Page<RoomEntity> rooms = roomRepository.findAll(PageRequest.of(page, limit));

        // Convertimos los centros médicos a DTOs
        List<RoomDTO> roomDTOS = rooms.getContent().stream()
                .map(c -> modelMapper.map(c, RoomDTO.class))
                .toList();

        return RoomsListResponse.builder()
                .count(rooms.getTotalElements())
                .rooms(roomDTOS)
                .build();
    }

    @Transactional(readOnly = true)
    public RoomsListResponse searchRooms(String search, Optional<Integer> optionalPage, Optional<Integer> optionalLimit) {
        Integer page = Math.max(optionalPage.orElse(DEFAULT_PAGE), DEFAULT_PAGE);
        Integer limit = Math.min(optionalLimit.orElse(DEFAULT_PAGE_SIZE), MAX_ROWS_PER_PAGE);

        Page<RoomEntity> rooms = roomRepository.findByNameContainingIgnoreCaseOrMedicalCenter_NameContainingIgnoreCase(search, search, PageRequest.of(page, limit));

        // Convertimos los centros médicos a DTOs
        List<RoomDTO> roomDTOS = rooms.getContent().stream()
                .map(c -> modelMapper.map(c, RoomDTO.class))
                .toList();

        return RoomsListResponse.builder()
                .count(rooms.getTotalElements())
                .rooms(roomDTOS)
                .build();
    }

    @Transactional(readOnly = true)
    public RoomDTO getRoom(Integer id) {
        RoomEntity consulta = roomRepository.findById(id).orElseThrow(DataNotFoundException::roomNotFound);
        return modelMapper.map(consulta, RoomDTO.class);
    }

    @Transactional
    public RoomDTO addRoom(RoomRequest roomRequest) {
        // Busqueda del centro medico
        MedicalCenterEntity centroMedico = medicalCenterRepository.findById(roomRequest.getMedicalCenter()).orElseThrow(DataNotFoundException::medicalCenterNotFound);

        // Mapeo de la entidad consulta y asignación del centro medico
        RoomEntity room = modelMapper.map(roomRequest, RoomEntity.class);
        room.setMedicalCenter(centroMedico);

        // Guardado en la base de datos
        room = roomRepository.save(room);

        // Devolvemos el DTO de la consulta
        return modelMapper.map(room, RoomDTO.class);
    }

    @Transactional
    public RoomDTO updateRoom(Integer id, RoomRequest roomRequest) {
        MedicalCenterEntity medicalCenter = medicalCenterRepository.findById(roomRequest.getMedicalCenter()).orElseThrow(DataNotFoundException::medicalCenterNotFound);
        RoomEntity room = roomRepository.findById(id).orElseThrow(DataNotFoundException::roomNotFound);

        modelMapper.map(roomRequest, room);
        room.setMedicalCenter(medicalCenter);

        room = roomRepository.save(room);

        return modelMapper.map(room, RoomDTO.class);
    }

    @Transactional
    public void deleteRoom(Integer id) {
        roomRepository.deleteById(id);
    }
}