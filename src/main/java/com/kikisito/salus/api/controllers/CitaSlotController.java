package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.CitaSlotDTO;
import com.kikisito.salus.api.dto.request.CitaSlotRequest;
import com.kikisito.salus.api.dto.request.GetCitaSlotRequest;
import com.kikisito.salus.api.service.CitaSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/cita-slot")
@RequiredArgsConstructor
@CrossOrigin
public class CitaSlotController {
    @Autowired
    private final CitaSlotService citaSlotService;

    @GetMapping("/{medicoId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<CitaSlotDTO>> getAgendasMedico(@PathVariable Integer medicoId, @RequestBody GetCitaSlotRequest getCitaSlotRequest) {
        return ResponseEntity.ok(citaSlotService.getCitasSlotByMedicoAndFecha(medicoId, getCitaSlotRequest.getFecha()));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CitaSlotDTO> createCitaSlot(@RequestBody CitaSlotRequest citaSlotRequest) {
        return ResponseEntity.ok(citaSlotService.createCitaSlot(citaSlotRequest));
    }

    @DeleteMapping("/{citaSlot}/delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteCitaSlot(@PathVariable("citaSlot") Integer citaSlotId) {
        citaSlotService.deleteCitaSlot(citaSlotId);
        return ResponseEntity.ok().build();
    }
}
