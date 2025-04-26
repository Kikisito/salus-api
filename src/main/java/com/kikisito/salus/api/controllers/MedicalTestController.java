package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.MedicalTestDTO;
import com.kikisito.salus.api.dto.request.MedicalTestRequest;
import com.kikisito.salus.api.service.MedicalTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/medical-tests")
@RequiredArgsConstructor
@CrossOrigin
public class MedicalTestController {
    @Autowired
    private final MedicalTestService medicalTestService;
    
    @GetMapping(value = "/{medicalTestId}/pdf", produces = "application/pdf")
    @PreAuthorize(
            "hasAuthority('ADMIN')" +
                    "or (hasAuthority('PROFESSIONAL') " +
                        "and @medicalTestService.isDoctorResponsibleOfMedicalTest(#medicalTestId, authentication.principal.medicalProfile.id))"
    )
    public ResponseEntity<byte[]> getReportPdf(@PathVariable Integer medicalTestId) {
        return ResponseEntity.ok(medicalTestService.getMedicalTestPdf(medicalTestId));
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(
            "hasAuthority('ADMIN')" +
            "or (hasAuthority('PROFESSIONAL') " +
                    "and (#medicalTestRequest.appointment == null or @appointmentService.canProfessionalAccessAppointment(#medicalTestRequest.appointment, authentication.principal.medicalProfile.id))" +
                    "and (#medicalTestRequest.doctor == authentication.principal.medicalProfile.id))"
    )
    public ResponseEntity<MedicalTestDTO> addMedicalTest(
            @RequestPart @Valid MedicalTestRequest medicalTestRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
            ) {
        return ResponseEntity.ok(medicalTestService.addMedicalTest(medicalTestRequest, files));
    }

    @DeleteMapping("/{medicalTestId}")
    @PreAuthorize(
            "hasAuthority('ADMIN')" +
            "or (hasAuthority('PROFESSIONAL') and @medicalTestService.isDoctorResponsibleOfMedicalTest(#medicalTestId, authentication.principal.medicalProfile.id))"
    )
    public ResponseEntity<Void> deleteMedicalTest(@PathVariable Integer medicalTestId) {
        medicalTestService.deleteMedicalTest(medicalTestId);
        return ResponseEntity.noContent().build();
    }
}