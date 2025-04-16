package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.RoomDTO;
import com.kikisito.salus.api.dto.request.RoomRequest;
import com.kikisito.salus.api.dto.response.RoomsListResponse;
import com.kikisito.salus.api.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/rooms")
@RequiredArgsConstructor
@CrossOrigin
public class RoomController {
    @Autowired
    private final RoomService roomService;

    @GetMapping(value = { "/all", "/all/{page}", "/all/{page}/{limit}"})
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<RoomsListResponse> getRooms(@PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(roomService.getRooms(page, limit));
    }

    @GetMapping(value = { "/search/{search}", "/search/{search}/{page}", "/search/{search}/{page}/{limit}"})
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<RoomsListResponse> searchRooms(@PathVariable String search, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(roomService.searchRooms(search, page, limit));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RoomDTO> addRoom(@RequestBody @Valid RoomRequest roomRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.addRoom(roomRequest));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(roomService.getRoom(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable("id") Integer id, @RequestBody @Valid RoomRequest roomRequest) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") Integer id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
