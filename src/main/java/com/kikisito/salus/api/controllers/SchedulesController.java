package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.DoctorScheduleDTO;
import com.kikisito.salus.api.dto.request.DoctorScheduleRequest;
import com.kikisito.salus.api.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("api/v1/schedules")
@RequiredArgsConstructor
@CrossOrigin
public class SchedulesController {
    @Autowired
    private final DoctorScheduleService doctorScheduleService;

    @GetMapping("/{doctorId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<DoctorScheduleDTO>> getDoctorSchedules(@PathVariable Integer doctorId) {
        return ResponseEntity.ok(doctorScheduleService.getDoctorSchedules(doctorId));
    }

    @GetMapping("/{doctorId}/{diaSemana}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<DoctorScheduleDTO>> getDoctorSchedulesByDayOfWeek(@PathVariable Integer doctorId, @PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(doctorScheduleService.getDoctorSchedulesByDayOfWeek(doctorId, dayOfWeek));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DoctorScheduleDTO> addScheduleEntry(@RequestBody @Valid DoctorScheduleRequest doctorScheduleRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorScheduleService.addSchedule(doctorScheduleRequest));
    }

    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DoctorScheduleDTO> updateScheduleEntry(@PathVariable("scheduleId") Integer scheduleId, @RequestBody @Valid DoctorScheduleRequest doctorScheduleRequest) {
        return ResponseEntity.ok(doctorScheduleService.updateSchedule(scheduleId, doctorScheduleRequest));
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteScheduleEntry(@PathVariable Integer scheduleId) {
        doctorScheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
}
