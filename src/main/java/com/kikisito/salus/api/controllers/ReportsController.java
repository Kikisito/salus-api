package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.ReportDTO;
import com.kikisito.salus.api.dto.request.ReportRequest;
import com.kikisito.salus.api.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/reports")
@RequiredArgsConstructor
@CrossOrigin
public class ReportsController {
    @Autowired
    private final ReportService reportService;

    @GetMapping("/by-appointment/{appointmentId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and @appointmentService.canProfessionalAccessAppointment(#appointmentId, authentication.principal.medicalProfile.id))")
    public ResponseEntity<List<ReportDTO>> getUserReportsByAppointment(@PathVariable Integer appointmentId) {
        return ResponseEntity.ok(reportService.getAppointmentReports(appointmentId));
    }

    @GetMapping("/patient/{patientId}/doctor/{doctorId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and #doctorId == authentication.principal.medicalProfile.id)")
    public ResponseEntity<List<ReportDTO>> getPatientAppointmentsWithDoctorOrItsSpecialties(
            @PathVariable Integer patientId,
            @PathVariable Integer doctorId
    ) {
        return ResponseEntity.ok(reportService.getPatientReportsWithDoctorOrItsSpecialties(patientId, doctorId));
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and @reportService.canProfessionalAccessReport(#reportId, authentication.principal.medicalProfile.id))")
    public ResponseEntity<ReportDTO> getReport(@PathVariable Integer reportId) {
        return ResponseEntity.ok(reportService.getReport(reportId));
    }

    @GetMapping(value = "/{reportId}/pdf", produces = "application/pdf")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and @reportService.canProfessionalAccessReport(#reportId, authentication.principal.medicalProfile.id))")
    public ResponseEntity<byte[]> getReportPdf(@PathVariable Integer reportId) {
        return ResponseEntity.ok(reportService.getReportPdf(reportId));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN') " +
            "or (" +
                "hasAuthority('PROFESSIONAL') " +
                "and (#request.appointment == null or @appointmentService.canProfessionalAccessAppointment(#request.appointment, authentication.principal.medicalProfile.id) and #request.doctor == authentication.principal.medicalProfile.id)" +
            ")"
    )
    public ResponseEntity<ReportDTO> addReport(@RequestBody @Valid ReportRequest request) {
        return ResponseEntity.ok(reportService.addReport(request));
    }

    @PutMapping("/{reportId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and @reportService.canProfessionalAccessReport(#reportId, authentication.principal.medicalProfile.id))")
    public ResponseEntity<ReportDTO> updateReport(@PathVariable Integer reportId, @RequestBody @Valid ReportRequest request) {
        return ResponseEntity.ok(reportService.updateReport(reportId, request));
    }

    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and @reportService.canProfessionalAccessReport(#reportId, authentication.principal.medicalProfile.id))")
    public ResponseEntity<Void> deleteReport(@PathVariable Integer reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }
}
