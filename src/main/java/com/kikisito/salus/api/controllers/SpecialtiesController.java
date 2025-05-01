package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.SpecialtyDTO;
import com.kikisito.salus.api.dto.request.AddSpecialtyRequest;
import com.kikisito.salus.api.dto.response.SpecialtiesListResponse;
import com.kikisito.salus.api.service.SpecialtyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/specialties")
@RequiredArgsConstructor
@CrossOrigin
public class SpecialtiesController {
    @Autowired
    private final SpecialtyService perfilMedicoService;

    @GetMapping(value = { "/all", "/all/{page}", "/all/{page}/{limit}"})
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<SpecialtiesListResponse> getAllSpecialties(@PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(perfilMedicoService.getAllSpecialties(page, limit));
    }

    @GetMapping(value = { "/search/{search}", "/search/{search}/{page}", "/search/{search}/{page}/{limit}"})
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<SpecialtiesListResponse> searchSpecialties(@PathVariable String search, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(perfilMedicoService.searchSpecialties(search, page, limit));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialtyDTO> addSpecialty(@RequestBody @Valid AddSpecialtyRequest addSpecialtyRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilMedicoService.addSpecialty(addSpecialtyRequest));
    }

    @GetMapping("/{specialtyId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialtyDTO> getSpecialty(@PathVariable("specialtyId") Integer id) {
        return ResponseEntity.ok(perfilMedicoService.getSpecialty(id));
    }

    @PutMapping("/{specialtyId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialtyDTO> updateSpecialty(@PathVariable("specialtyId") Integer id, @RequestBody @Valid AddSpecialtyRequest addSpecialtyRequest) {
        return ResponseEntity.ok(perfilMedicoService.updateSpecialty(id, addSpecialtyRequest));
    }

    @DeleteMapping("/{specialtyId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteSpecialty(@PathVariable("specialtyId") Integer id) {
        perfilMedicoService.deleteSpecialty(id);
        return ResponseEntity.noContent().build();
    }
}
