package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.AppointmentDTO;
import com.kikisito.salus.api.dto.ReducedAppointmentDTO;
import com.kikisito.salus.api.dto.request.AppointmentStatusRequest;
import com.kikisito.salus.api.dto.request.ObservationsRequest;
import com.kikisito.salus.api.dto.request.AppointmentRequest;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.service.AppointmentService;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<ReducedAppointmentDTO>> getSessionUserUpcomingAppointments(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(appointmentService.getUserUpcomingAppointmentsReduced(user.getId()));
    }

    @GetMapping("/@me/past")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<ReducedAppointmentDTO>> getSessionUserPastAppointments(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(appointmentService.getUserPastAppointmentsReduced(user.getId()));
    }

    @PostMapping("/new")
    @PreAuthorize("""
                    (
                        hasAuthority('USER')
                        and #appointmentRequest.patient == authentication.principal.id
                        and authentication.principal.restricted == false
                        and @appointmentService.countAppointmentsByPatient(authentication.principal) < 5
                    ) or hasAuthority('ADMIN')
                """)
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody @Valid AppointmentRequest appointmentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(appointmentRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('ADMIN')" +
            "or (hasAuthority('USER') and @appointmentService.canUserDeleteAppointment(#id, authentication.principal))"
    )
    public ResponseEntity<Void> deleteAppointment(@PathVariable Integer id, @AuthenticationPrincipal UserEntity userRequest) {
        appointmentService.deleteAppointment(id, userRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/patient/{patientId}/doctor/{doctorId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and #doctorId == authentication.principal.medicalProfile.id)")
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointmentsWithDoctorOrItsSpecialties(
            @PathVariable Integer patientId,
            @PathVariable Integer doctorId
    ) {
        return ResponseEntity.ok(appointmentService.getPatientAppointmentsWithDoctorOrItsSpecialties(patientId, doctorId));
    }

    @GetMapping("/{id}")
    @PreAuthorize(  "hasAuthority('ADMIN')" +
                    "or (hasAuthority('PROFESSIONAL') and @appointmentService.canProfessionalAccessAppointment(#id, authentication.principal.medicalProfile.id))" +
                    "or (hasAuthority('USER') and @appointmentService.canUserAccessAppointment(#id, authentication.principal))")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and @appointmentService.canProfessionalAccessAppointment(#id, authentication.principal.medicalProfile.id))")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(@PathVariable Integer id, @RequestBody @Valid AppointmentStatusRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/doctor-observations")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and @appointmentService.canProfessionalAccessAppointment(#id, authentication.principal.medicalProfile.id))")
    public ResponseEntity<AppointmentDTO> updateAppointmentDoctorObservations(@PathVariable Integer id, @RequestBody @Valid ObservationsRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointmentDoctorObservations(id, request.getObservations()));
    }
}
