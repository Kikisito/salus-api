package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.ConsultaDTO;
import com.kikisito.salus.api.dto.request.ConsultaRequest;
import com.kikisito.salus.api.service.ConsultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/consulta")
@RequiredArgsConstructor
@CrossOrigin
public class ConsultaController {
    @Autowired
    private final ConsultaService consultaService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<ConsultaDTO>> getConsultas() {
        return ResponseEntity.ok(consultaService.getConsultas());
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ConsultaDTO> addConsulta(@RequestBody @Valid ConsultaRequest consultaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.addConsulta(consultaRequest));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ConsultaDTO> getConsulta(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(consultaService.getConsulta(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ConsultaDTO> updateConsulta(@PathVariable("id") Integer id, @RequestBody @Valid ConsultaRequest consultaRequest) {
        return ResponseEntity.ok(consultaService.updateConsulta(id, consultaRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteConsulta(@PathVariable("id") Integer id) {
        consultaService.deleteConsulta(id);
        return ResponseEntity.noContent().build();
    }
}
