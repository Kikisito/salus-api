package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.AppointmentDTO;
import com.kikisito.salus.api.dto.request.AppointmentRequest;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.service.AppointmentService;
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
public class AppointmentsController {
    @Autowired
    private final AppointmentService appointmentService;

    @GetMapping("/@me")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<AppointmentDTO>> getSessionUserAppointments(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(appointmentService.getAllUserAppointments(user.getId()));
    }

    @PostMapping("/@me/new")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<AppointmentDTO> createAppointment(@AuthenticationPrincipal UserEntity user, @RequestBody AppointmentRequest appointmentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(user, appointmentRequest));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }
}
