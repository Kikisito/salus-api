package com.kikisito.salus.api.service;

import com.kikisito.salus.api.entity.MedicoEntity;
import com.kikisito.salus.api.repository.AusenciaMedicoRepository;
import com.kikisito.salus.api.repository.CitaRepository;
import com.kikisito.salus.api.repository.HorarioMedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AgendaService {
    @Autowired
    private final CitaRepository citaRepository;

    @Autowired
    private final HorarioMedicoRepository horarioMedicoRepository;

    @Autowired
    private final AusenciaMedicoRepository ausenciaMedicoRepository;

    @Transactional(readOnly = true)
    public void getHorariosDisponibles(MedicoEntity medico, LocalDate fecha) {
    }
}