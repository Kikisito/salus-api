package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.EspecialidadDTO;
import com.kikisito.salus.api.dto.PerfilMedicoDTO;
import com.kikisito.salus.api.dto.request.AddEspecialidadMedicoRequest;
import com.kikisito.salus.api.dto.request.AddPerfilMedicoToUserRequest;
import com.kikisito.salus.api.service.PerfilMedicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/perfil-medico")
@RequiredArgsConstructor
@CrossOrigin
public class PerfilMedicoController {
    @Autowired
    private final PerfilMedicoService perfilMedicoService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PerfilMedicoDTO> addPerfilMedico(@RequestBody @Valid AddPerfilMedicoToUserRequest addPerfilMedicoToUserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilMedicoService.addMedicoFromUser(addPerfilMedicoToUserRequest));
    }

    @GetMapping("/{perfilId}/especialidades")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<EspecialidadDTO>> getEspecialidadesMedico(@PathVariable("perfilId") Integer id) {
        return ResponseEntity.ok(perfilMedicoService.getEspecialidades(id));
    }

    @PostMapping("/{medicoId}/especialidades/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PerfilMedicoDTO> addEspecialidadMedico(@PathVariable("medicoId") Integer medicoId, @RequestBody @Valid AddEspecialidadMedicoRequest addEspecialidadMedicoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilMedicoService.addEspecialidadMedico(medicoId, addEspecialidadMedicoRequest));
    }

    @DeleteMapping("/{medicoId}/especialidades/{especialidadId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PerfilMedicoDTO> deleteEspecialidadMedico(@PathVariable("medicoId") Integer medicoId, @PathVariable("especialidadId") Integer especialidadId) {
        return ResponseEntity.ok(perfilMedicoService.deleteEspecialidadMedico(medicoId, especialidadId));
    }
}
