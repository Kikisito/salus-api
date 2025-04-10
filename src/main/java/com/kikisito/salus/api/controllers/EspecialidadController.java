package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.EspecialidadDTO;
import com.kikisito.salus.api.dto.request.AddEspecialidadRequest;
import com.kikisito.salus.api.dto.response.SpecialtiesListResponse;
import com.kikisito.salus.api.service.EspecialidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/specialties")
@RequiredArgsConstructor
@CrossOrigin
public class EspecialidadController {
    @Autowired
    private final EspecialidadService perfilMedicoService;

    @GetMapping(value = { "/all", "/all/{page}", "/all/{page}/{limit}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialtiesListResponse> getAllEspecialidades(@PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(perfilMedicoService.getAllEspecialidades(page, limit));
    }

    @GetMapping(value = { "/search/{search}", "/search/{search}/{page}", "/search/{search}/{page}/{limit}"})
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<SpecialtiesListResponse> searchEspecialidad(@PathVariable String search, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        return ResponseEntity.ok(perfilMedicoService.searchEspecialidades(search, page, limit));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<EspecialidadDTO> addEspecialidad(@RequestBody @Valid AddEspecialidadRequest addEspecialidadRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilMedicoService.addEspecialidad(addEspecialidadRequest));
    }

    @GetMapping("/{especialidadId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<EspecialidadDTO> getEspecialidad(@PathVariable("especialidadId") Integer id) {
        return ResponseEntity.ok(perfilMedicoService.getEspecialidad(id));
    }

    @PutMapping("/{especialidadId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<EspecialidadDTO> updateEspecialidad(@PathVariable("especialidadId") Integer id, @RequestBody @Valid AddEspecialidadRequest addEspecialidadRequest) {
        return ResponseEntity.ok(perfilMedicoService.updateEspecialidad(id, addEspecialidadRequest));
    }

    @DeleteMapping("/{especialidadId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteEspecialidad(@PathVariable("especialidadId") Integer id) {
        perfilMedicoService.deleteEspecialidad(id);
        return ResponseEntity.noContent().build();
    }
}
