package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.CentroMedicoDTO;
import com.kikisito.salus.api.dto.request.NewCentroMedicoRequest;
import com.kikisito.salus.api.dto.response.MedicalCentersListResponse;
import com.kikisito.salus.api.service.CentroMedicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/medical-centers")
@RequiredArgsConstructor
@CrossOrigin
public class CentroMedicoController {
    @Autowired
    private final CentroMedicoService centroMedicoService;

    @GetMapping(value = { "/all", "/all/{page}", "/all/{page}/{limit}"})
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<MedicalCentersListResponse> getCurrentProfile(@PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(centroMedicoService.getCentrosMedicos(page, limit));
    }

    @GetMapping(value = { "/search/{search}", "/search/{search}/{page}", "/search/{search}/{page}/{limit}"})
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<MedicalCentersListResponse> searchCentroMedico(@PathVariable String search, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(centroMedicoService.searchCentrosMedicos(search, page, limit));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CentroMedicoDTO> addCentroMedico(@RequestBody @Valid NewCentroMedicoRequest centroMedicoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(centroMedicoService.addCentroMedico(centroMedicoDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<CentroMedicoDTO> getCentroMedicoById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(centroMedicoService.getCentroMedicoById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CentroMedicoDTO> updateCentroMedico(@PathVariable("id") Integer id, @RequestBody @Valid NewCentroMedicoRequest centroMedicoDTO) {
        return ResponseEntity.ok(centroMedicoService.updateCentroMedico(id, centroMedicoDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteCentroMedico(@PathVariable("id") Integer id) {
        centroMedicoService.deleteCentroMedico(id);
        return ResponseEntity.noContent().build();
    }
}
