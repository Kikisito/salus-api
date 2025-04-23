package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.AppointmentSlotDTO;
import com.kikisito.salus.api.dto.request.AppointmentSlotRequest;
import com.kikisito.salus.api.service.AppointmentSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/appointment-slots")
@RequiredArgsConstructor
@CrossOrigin
public class AppointmentSlotsController {
    @Autowired
    private final AppointmentSlotService appointmentSlotService;

    @GetMapping("/{doctorId}/{date}/daily")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and authentication.principal.medicalProfile.id == #doctorId)")
    public ResponseEntity<List<AppointmentSlotDTO>> getDoctorAppointmentSlots(@PathVariable Integer doctorId, @PathVariable @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date) {
        return ResponseEntity.ok(appointmentSlotService.getAppointmentSlotsByDoctorAndDate(doctorId, date));
    }

    @GetMapping("/{doctorId}/{date}/weekly")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and authentication.principal.medicalProfile.id == #doctorId)")
    public ResponseEntity<List<AppointmentSlotDTO>> getDoctorWeeklyAppointmentSlots(@PathVariable Integer doctorId, @PathVariable @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date) {
        return ResponseEntity.ok(appointmentSlotService.getWeeklyAppointmentSlotsByDoctorAndDate(doctorId, date));
    }

    @GetMapping("/{appointmentSlotId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AppointmentSlotDTO> getAppointmentSlot(@PathVariable Integer appointmentSlotId) {
        return ResponseEntity.ok(appointmentSlotService.getAppointmentSlot(appointmentSlotId));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AppointmentSlotDTO> createAppointmentSlot(@RequestBody @Valid AppointmentSlotRequest appointmentSlotRequest) {
        return ResponseEntity.ok(appointmentSlotService.createAppointmentSlot(appointmentSlotRequest));
    }

    
    @DeleteMapping("/{appointmentSlot}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteAppointmentSlot(@PathVariable("appointmentSlot") Integer appointmentSlotId) {
        appointmentSlotService.deleteAppointmentSlot(appointmentSlotId);
        return ResponseEntity.ok().build();
    }
}
