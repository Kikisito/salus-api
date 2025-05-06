package com.kikisito.salus.api.entity;

import com.kikisito.salus.api.type.AppointmentStatusType;
import com.kikisito.salus.api.type.AppointmentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "appointments")
public class AppointmentEntity extends DatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private AppointmentSlotEntity slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private UserEntity patient;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by", nullable = false)
    private UserEntity lastModifiedBy;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppointmentType type = AppointmentType.IN_PERSON;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppointmentStatusType status = AppointmentStatusType.PENDING;

    @Column
    private String reason;

    @Column
    private String doctorObservations;

    @OneToMany(mappedBy = "appointment", fetch = FetchType.LAZY)
    private List<ReportEntity> reports;

    @OneToMany(mappedBy = "appointment", fetch = FetchType.LAZY)
    private List<PrescriptionEntity> prescriptions;

    @OneToMany(mappedBy = "appointment", fetch = FetchType.LAZY)
    private List<MedicalTestEntity> medicalTests;
}
