package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.AppointmentSlotDTO;
import com.kikisito.salus.api.dto.request.AppointmentSlotRequest;
import com.kikisito.salus.api.dto.request.GetAppointmentSlotRequest;
import com.kikisito.salus.api.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/appointment-slots")
@RequiredArgsConstructor
@CrossOrigin
public class AppointmentSlotsController {
    @Autowired
    private final AppointmentSlotService appointmentSlotService;

    @GetMapping("/{medicoId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AppointmentSlotDTO>> getDoctorAppointmentSlots(@PathVariable Integer medicoId, @RequestBody GetAppointmentSlotRequest getAppointmentSlotRequest) {
        return ResponseEntity.ok(appointmentSlotService.getAppointmentSlotsByDoctorAndDate(medicoId, getAppointmentSlotRequest.getDate()));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AppointmentSlotDTO> createAppointmentSlot(@RequestBody AppointmentSlotRequest appointmentSlotRequest) {
        return ResponseEntity.ok(appointmentSlotService.createAppointmentSlot(appointmentSlotRequest));
    }

    
    @DeleteMapping("/{citaSlot}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteAppointmentSlot(@PathVariable("citaSlot") Integer appointmentSlotId) {
        appointmentSlotService.deleteAppointmentSlot(appointmentSlotId);
        return ResponseEntity.ok().build();
    }
}
