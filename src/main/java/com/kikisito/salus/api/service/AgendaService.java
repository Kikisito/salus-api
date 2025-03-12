package com.kikisito.salus.api.service;

import com.kikisito.salus.api.repository.AusenciaMedicoRepository;
import com.kikisito.salus.api.repository.CitaRepository;
import com.kikisito.salus.api.repository.HorarioMedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgendaService {
    @Autowired
    private final CitaRepository citaRepository;

    @Autowired
    private final HorarioMedicoRepository horarioMedicoRepository;

    @Autowired
    private final AusenciaMedicoRepository ausenciaMedicoRepository;
}