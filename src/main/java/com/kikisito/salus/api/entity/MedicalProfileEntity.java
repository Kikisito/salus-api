package com.kikisito.salus.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@SuperBuilder
@Table(name = "medical_profiles")
public class MedicalProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(nullable = false, unique = true)
    private String license;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "doctor_specialties",
            joinColumns = @JoinColumn(name = "doctor_id"),
            foreignKey = @ForeignKey(name = "fk_doctor_specialty__doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id"),
            inverseForeignKey = @ForeignKey(name = "fk_doctor_specialty__specialty_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "specialty_id"})
    )
    private List<SpecialtyEntity> specialties;

    @OneToMany(mappedBy = "doctor")
    private List<AppointmentSlotEntity> appointmentSlots;

    @OneToMany(mappedBy = "doctor")
    private List<DoctorScheduleEntity> schedules;

    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    private List<ReportEntity> reports;
}