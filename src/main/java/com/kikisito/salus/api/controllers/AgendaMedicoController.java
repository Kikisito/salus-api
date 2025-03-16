package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.AgendaMedicoDTO;
import com.kikisito.salus.api.dto.request.AgendaMedicoRequest;
import com.kikisito.salus.api.service.AgendaMedicoService;
import com.kikisito.salus.api.type.DiaSemana;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/agenda")
@RequiredArgsConstructor
@CrossOrigin
public class AgendaMedicoController {
    @Autowired
    private final AgendaMedicoService agendaMedicoService;

    @GetMapping("/{medicoId}/{diaSemana}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AgendaMedicoDTO>> getAgendasMedico(@PathVariable Integer medicoId, @PathVariable DiaSemana diaSemana) {
        return ResponseEntity.ok(agendaMedicoService.getAgendasMedicoByDiaSemana(medicoId, diaSemana));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AgendaMedicoDTO> addAgendaEntry(@RequestBody @Valid AgendaMedicoRequest agendaMedicoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendaMedicoService.addAgendaMedico(agendaMedicoRequest));
    }

    @PutMapping("/update/{agendaId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AgendaMedicoDTO> updateAgendaEntry(@PathVariable("agendaId") Integer agendaId, @RequestBody @Valid AgendaMedicoRequest agendaMedicoRequest) {
        return ResponseEntity.ok(agendaMedicoService.updateAgendaMedico(agendaId, agendaMedicoRequest));
    }

    @DeleteMapping("/delete/{agendaId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteAgendaEntry(@PathVariable Integer agendaId) {
        agendaMedicoService.deleteAgendaMedico(agendaId);
        return ResponseEntity.noContent().build();
    }
}
