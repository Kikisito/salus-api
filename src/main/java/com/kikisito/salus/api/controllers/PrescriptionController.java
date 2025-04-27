package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.PrescriptionDTO;
import com.kikisito.salus.api.dto.request.PrescriptionRequest;
import com.kikisito.salus.api.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/prescriptions")
@RequiredArgsConstructor
@CrossOrigin
public class PrescriptionController {
    @Autowired
    private final PrescriptionService prescriptionService;

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize(
            "hasAuthority('ADMIN') or " +
            "(hasAuthority('PROFESSIONAL') and @appointmentService.canProfessionalAccessAppointment(#appointmentId, authentication.principal.medicalProfile.id))"
    )
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByAppointment(@PathVariable Integer appointmentId) {
        List<PrescriptionDTO> prescriptions = prescriptionService.getAppointmentPrescriptions(appointmentId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize(
            "hasAuthority('ADMIN') or " +
            "(hasAuthority('PROFESSIONAL') and #doctorId == authentication.principal.medicalProfile.id)"
    )
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByDoctor(@PathVariable Integer doctorId) {
        List<PrescriptionDTO> prescriptions = prescriptionService.getDoctorPrescriptions(doctorId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize(
            "hasAuthority('ADMIN') or " +
            "(hasAuthority('USER') and #patientId == authentication.principal.id)"
    )
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByPatient(@PathVariable Integer patientId) {
        List<PrescriptionDTO> prescriptions = prescriptionService.getPatientPrescriptions(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/patient/{patientId}/doctor/{doctorId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and #doctorId == authentication.principal.medicalProfile.id)")
    public ResponseEntity<List<PrescriptionDTO>> getPatientPrescriptionsWithDoctorOrItsSpecialties(
            @PathVariable Integer patientId,
            @PathVariable Integer doctorId
    ) {
        return ResponseEntity.ok(prescriptionService.getPatientPrescriptionsWithDoctorOrItsSpecialties(patientId, doctorId));
    }

    @GetMapping(value = "/{prescriptionId}/pdf", produces = "application/pdf")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and (hasAuthority('PROFESSIONAL') and @prescriptionService.canProfessionalAccessPrescription(#prescriptionId, authentication.principal.medicalProfile.id)))")
    public ResponseEntity<byte[]> getPrescriptionPdf(@PathVariable Integer prescriptionId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionPdf(prescriptionId));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN') " +
            "or (" +
                "hasAuthority('PROFESSIONAL') " +
                "and (#request.appointment == null or @appointmentService.canProfessionalAccessAppointment(#request.appointment, authentication.principal.medicalProfile.id) and #request.doctor == authentication.principal.medicalProfile.id)" +
            ")"
    )
    public ResponseEntity<PrescriptionDTO> addPrescription(@RequestBody @Valid PrescriptionRequest request) {
        PrescriptionDTO prescription = prescriptionService.addPrescription(request);
        return ResponseEntity.ok(prescription);
    }

    @PutMapping("/{prescriptionId}")
    @PreAuthorize(
            "hasAuthority('ADMIN') or " +
            "(hasAuthority('PROFESSIONAL') and @prescriptionService.canProfessionalAccessPrescription(#prescriptionId, authentication.principal.medicalProfile.id))"
    )
    public ResponseEntity<PrescriptionDTO> updatePrescription(@PathVariable Integer prescriptionId, @RequestBody @Valid PrescriptionRequest request) {
        PrescriptionDTO prescription = prescriptionService.updatePrescription(prescriptionId, request);
        return ResponseEntity.ok(prescription);
    }

    @DeleteMapping("/{prescriptionId}")
    @PreAuthorize(
            "hasAuthority('ADMIN') or " +
            "(hasAuthority('PROFESSIONAL') and @prescriptionService.canProfessionalAccessPrescription(#prescriptionId, authentication.principal.medicalProfile.id))"
    )
    public ResponseEntity<Void> deletePrescription(@PathVariable Integer prescriptionId) {
        prescriptionService.deletePrescription(prescriptionId);
        return ResponseEntity.noContent().build();
    }
}
