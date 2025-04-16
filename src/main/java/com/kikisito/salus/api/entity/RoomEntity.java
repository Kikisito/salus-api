package com.kikisito.salus.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "rooms")
public class RoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_center_id", nullable = false)
    private MedicalCenterEntity medicalCenter;

    @Column(nullable = false)
    private String name;

    // Cascade para borrar citas
    // antes de borrarlas se debería ver si se notifican usuarios o se hace otra acción
    @OneToMany(mappedBy = "room")
    private List<AppointmentSlotEntity> appointments;

    @OneToMany(mappedBy = "room")
    private List<DoctorScheduleEntity> agendas;
}
