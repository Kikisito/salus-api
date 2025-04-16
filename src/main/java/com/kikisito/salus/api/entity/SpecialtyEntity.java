package com.kikisito.salus.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "specialties")
public class SpecialtyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @ManyToMany(mappedBy = "specialties")
    private List<MedicalProfileEntity> doctors;

    @OneToMany(mappedBy = "specialty")
    private List<AppointmentSlotEntity> slots;

    @OneToMany(mappedBy = "specialty")
    private List<DoctorScheduleEntity> schedules;
}