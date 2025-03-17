package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.CitaDTO;
import com.kikisito.salus.api.dto.request.CitaRequest;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/appointments")
@RequiredArgsConstructor
@CrossOrigin
public class CitasController {
    @Autowired
    private final CitaService citaService;

    @GetMapping("/@me")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<CitaDTO>> getSessionUserAppointments(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(citaService.getAllUserAppointments(user.getId()));
    }

    @PostMapping("/@me/new")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<CitaDTO> createAppointment(@AuthenticationPrincipal UserEntity user, @RequestBody CitaRequest citaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.obtainAppointment(user, citaRequest));
    }
}
