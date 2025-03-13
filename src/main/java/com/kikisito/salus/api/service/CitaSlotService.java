package com.kikisito.salus.api.service;

import com.kikisito.salus.api.repository.AusenciaMedicoRepository;
import com.kikisito.salus.api.repository.CitaRepository;
import com.kikisito.salus.api.repository.AgendaMedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CitaSlotService {
    @Autowired
    private final CitaRepository citaRepository;

    @Autowired
    private final AgendaMedicoRepository agendaMedicoRepository;

    @Autowired
    private final AusenciaMedicoRepository ausenciaMedicoRepository;
}