package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.PerfilMedicoDTO;
import com.kikisito.salus.api.dto.request.AddPerfilMedicoToUserRequest;
import com.kikisito.salus.api.service.PerfilMedicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/perfil-medico")
@RequiredArgsConstructor
@CrossOrigin
public class PerfilMedicoController {
    @Autowired
    private final PerfilMedicoService perfilMedicoService;

    @PostMapping("/addExisting")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PerfilMedicoDTO> addMedicoFromUser(@RequestBody @Valid AddPerfilMedicoToUserRequest addPerfilMedicoToUserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilMedicoService.addMedicoFromUser(addPerfilMedicoToUserRequest));
    }
}
