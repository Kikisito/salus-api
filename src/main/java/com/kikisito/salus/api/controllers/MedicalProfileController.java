package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.SpecialtyDTO;
import com.kikisito.salus.api.dto.MedicalProfileDTO;
import com.kikisito.salus.api.dto.request.AddDoctorSpecialtyRequest;
import com.kikisito.salus.api.dto.request.DoctorLicenseRequest;
import com.kikisito.salus.api.dto.response.DoctorsListResponse;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.service.MedicalProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/doctor-profiles")
@RequiredArgsConstructor
@CrossOrigin
public class MedicalProfileController {
    @Autowired
    private final MedicalProfileService medicalProfileService;

    private static final int MAX_ROWS_PER_PAGE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @GetMapping(value = { "/all", "/all/{page}", "/all/{page}/{limit}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DoctorsListResponse> getAllDoctors(@PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        // Validación antes de realizar la consulta
        if(page.isPresent() && page.get() < 0) {
            return ResponseEntity.badRequest().build();
        }

        // Limitamos el número de usuarios a mostrar en [1,MAX_ROWS_PER_PAGE] para evitar problemas de rendimiento
        if(limit.isPresent() && (limit.get() < 1 || limit.get() > MAX_ROWS_PER_PAGE)) {
            return ResponseEntity.badRequest().build();
        }

        // Obtenemos la lista de usuarios
        DoctorsListResponse doctors;
        if(page.isPresent() && limit.isPresent()) {
            doctors = medicalProfileService.getMedicalProfiles(page.get(), limit.get());
        } else if(page.isPresent()) {
            doctors = medicalProfileService.getMedicalProfiles(page.get(), DEFAULT_PAGE_SIZE);
        } else {
            doctors = medicalProfileService.getMedicalProfiles(0, DEFAULT_PAGE_SIZE);
        }
        return ResponseEntity.ok(doctors);
    }

    @GetMapping(value = { "/search/{search}", "/search/{search}/{page}", "/search/{search}/{page}/{limit}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DoctorsListResponse> searchDoctors(@PathVariable String search, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        // Validación antes de realizar la consulta
        if(page.isPresent() && page.get() < 0) {
            return ResponseEntity.badRequest().build();
        }

        // Limitamos el número de usuarios a mostrar en [1,MAX_ROWS_PER_PAGE] para evitar problemas de rendimiento
        if(limit.isPresent() && (limit.get() < 1 || limit.get() > MAX_ROWS_PER_PAGE)) {
            return ResponseEntity.badRequest().build();
        }

        // Obtenemos la lista de usuarios
        DoctorsListResponse doctors;
        if(page.isPresent() && limit.isPresent()) {
            doctors = medicalProfileService.searchMedicalProfiles(search, page.get(), limit.get());
        } else if(page.isPresent()) {
            doctors = medicalProfileService.searchMedicalProfiles(search, page.get(), DEFAULT_PAGE_SIZE);
        } else {
            doctors = medicalProfileService.searchMedicalProfiles(search, 0, DEFAULT_PAGE_SIZE);
        }
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/medical-center/{medicalCenterId}/specialty/{specialtyId}/available")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<MedicalProfileDTO>> getAvailableDoctors(@PathVariable Integer medicalCenterId, @PathVariable Integer specialtyId) {
        return ResponseEntity.ok(medicalProfileService.getMedicalProfilesByMedicalCenterSpecialtyAndHasAvailability(medicalCenterId, specialtyId));
    }

    @GetMapping("/{doctorId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MedicalProfileDTO> getMedicalProfile(@PathVariable("doctorId") Integer id) {
        return ResponseEntity.ok(medicalProfileService.getMedicalProfile(id));
    }

    @PutMapping("/{doctorId}/license")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MedicalProfileDTO> updateMedicalProfile(@PathVariable("doctorId") Integer id, @RequestBody @Valid DoctorLicenseRequest doctorLicenseRequest) {
        return ResponseEntity.ok(medicalProfileService.changeLicense(id, doctorLicenseRequest));
    }

    @GetMapping("/{doctorId}/specialties")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<SpecialtyDTO>> getDoctorSpecialties(@PathVariable("doctorId") Integer id) {
        return ResponseEntity.ok(medicalProfileService.getSpecialties(id));
    }

    @PostMapping("/{doctorId}/specialties/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MedicalProfileDTO> addSpecialtyToDoctor(@PathVariable("doctorId") Integer medicoId, @RequestBody @Valid AddDoctorSpecialtyRequest addDoctorSpecialtyRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalProfileService.addSpecialtyToMedicalProfile(medicoId, addDoctorSpecialtyRequest));
    }

    @DeleteMapping("/{doctorId}/specialties/{specialtyId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MedicalProfileDTO> deleteSpecialtyFromDoctor(@PathVariable("doctorId") Integer medicoId, @PathVariable("specialtyId") Integer specialtyId) {
        return ResponseEntity.ok(medicalProfileService.removeSpecialtyFromMedicalProfile(medicoId, specialtyId));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MedicalProfileDTO> addMedicalProfile(@RequestBody @Valid DoctorLicenseRequest doctorLicenseRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalProfileService.createMedicalProfileFromUserEntity(doctorLicenseRequest));
    }

    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteMedicalProfile(@PathVariable("doctorId") Integer id) {
        medicalProfileService.deleteMedicalProfile(id);
        return ResponseEntity.noContent().build();
    }

    // Apartado: PROFESSIONAL
    @GetMapping("/@me")
    @PreAuthorize("hasAuthority('PROFESSIONAL')")
    public ResponseEntity<MedicalProfileDTO> getMyMedicalProfile(@AuthenticationPrincipal UserEntity userEntity) {
        return ResponseEntity.ok(medicalProfileService.getMedicalProfileFromUserEntity(userEntity));
    }
}
