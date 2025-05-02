package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.MedicalCenterDTO;
import com.kikisito.salus.api.dto.request.NewMedicalCenterRequest;
import com.kikisito.salus.api.dto.response.MedicalCentersListResponse;
import com.kikisito.salus.api.service.MedicalCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/medical-centers")
@RequiredArgsConstructor
@CrossOrigin
public class MedicalCentersController {
    @Autowired
    private final MedicalCenterService medicalCenterService;

    @GetMapping(value = { "/all", "/all/{page}", "/all/{page}/{limit}"})
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<MedicalCentersListResponse> getCurrentProfile(@PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(medicalCenterService.getMedicalCenters(page, limit));
    }

    @GetMapping(value = { "/search/{search}", "/search/{search}/{page}", "/search/{search}/{page}/{limit}"})
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<MedicalCentersListResponse> searchMedicalCenter(@PathVariable String search, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(medicalCenterService.searchMedicalCenters(search, page, limit));
    }

    @GetMapping(value = {
            "/specialty/{specialtyId}/available",
            "/specialty/{specialtyId}/available/page/{page}",
            "/specialty/{specialtyId}/available/page/{page}/limit/{limit}"
    })
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<MedicalCentersListResponse> getAvailableMedicalCenters(@PathVariable Integer specialtyId, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(medicalCenterService.getMedicalCentersByAvailableSpecialty(specialtyId, page, limit));
    }

    @GetMapping(value = {
            "/specialty/{specialtyId}/available/search/{search}",
            "/specialty/{specialtyId}/available/search/{search}/page/{page}",
            "/specialty/{specialtyId}/available/search/{search}/page/{page}/limit/{limit}"
    })
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<MedicalCentersListResponse> searchAvailableMedicalCenters(@PathVariable Integer specialtyId, @PathVariable String search, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(medicalCenterService.searchMedicalCentersByAvailableSpecialty(specialtyId, search, page, limit));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MedicalCenterDTO> addMedicalCenter(@RequestBody @Valid NewMedicalCenterRequest centroMedicoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalCenterService.addMedicalCenter(centroMedicoDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<MedicalCenterDTO> getMedicalCenterById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(medicalCenterService.getMedicalCenterById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MedicalCenterDTO> updateMedicalCenter(@PathVariable("id") Integer id, @RequestBody @Valid NewMedicalCenterRequest centroMedicoDTO) {
        return ResponseEntity.ok(medicalCenterService.updateMedicaslCenter(id, centroMedicoDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteMedicalCenter(@PathVariable("id") Integer id) {
        medicalCenterService.deleteMedicalCenter(id);
        return ResponseEntity.noContent().build();
    }
}
