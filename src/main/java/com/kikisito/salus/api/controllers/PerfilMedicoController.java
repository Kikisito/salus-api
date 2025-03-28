package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.EspecialidadDTO;
import com.kikisito.salus.api.dto.PerfilMedicoDTO;
import com.kikisito.salus.api.dto.request.AddEspecialidadMedicoRequest;
import com.kikisito.salus.api.dto.request.AddPerfilMedicoToUserRequest;
import com.kikisito.salus.api.dto.response.DoctorsListResponse;
import com.kikisito.salus.api.service.PerfilMedicoService;
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
@RequestMapping("api/v1/doctor-profiles")
@RequiredArgsConstructor
@CrossOrigin
public class PerfilMedicoController {
    @Autowired
    private final PerfilMedicoService perfilMedicoService;

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
            doctors = perfilMedicoService.getPerfilesMedicos(page.get(), limit.get());
        } else if(page.isPresent()) {
            doctors = perfilMedicoService.getPerfilesMedicos(page.get(), DEFAULT_PAGE_SIZE);
        } else {
            doctors = perfilMedicoService.getPerfilesMedicos(0, DEFAULT_PAGE_SIZE);
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
            doctors = perfilMedicoService.searchPerfilesMedicos(search, page.get(), limit.get());
        } else if(page.isPresent()) {
            doctors = perfilMedicoService.searchPerfilesMedicos(search, page.get(), DEFAULT_PAGE_SIZE);
        } else {
            doctors = perfilMedicoService.searchPerfilesMedicos(search, 0, DEFAULT_PAGE_SIZE);
        }
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PerfilMedicoDTO> getPerfilMedico(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(perfilMedicoService.getPerfilMedico(id));
    }

    @GetMapping("/{perfilId}/specialties")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<EspecialidadDTO>> getEspecialidadesMedico(@PathVariable("perfilId") Integer id) {
        return ResponseEntity.ok(perfilMedicoService.getEspecialidades(id));
    }

    @PostMapping("/{medicoId}/specialties/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PerfilMedicoDTO> addEspecialidadMedico(@PathVariable("medicoId") Integer medicoId, @RequestBody @Valid AddEspecialidadMedicoRequest addEspecialidadMedicoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilMedicoService.addEspecialidadMedico(medicoId, addEspecialidadMedicoRequest));
    }

    @DeleteMapping("/{medicoId}/specialties/{especialidadId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PerfilMedicoDTO> deleteEspecialidadMedico(@PathVariable("medicoId") Integer medicoId, @PathVariable("especialidadId") Integer especialidadId) {
        return ResponseEntity.ok(perfilMedicoService.deleteEspecialidadMedico(medicoId, especialidadId));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PerfilMedicoDTO> addPerfilMedico(@RequestBody @Valid AddPerfilMedicoToUserRequest addPerfilMedicoToUserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilMedicoService.addMedicoFromUser(addPerfilMedicoToUserRequest));
    }
}
