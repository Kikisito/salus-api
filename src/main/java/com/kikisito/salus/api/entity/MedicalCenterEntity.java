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
@Table(name = "medical_centers")
public class MedicalCenterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String addressLine1;

    @Column
    private String addressLine2;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String municipality;

    @Column(nullable = false)
    private String locality;

    @OneToMany(mappedBy = "medicalCenter", cascade = CascadeType.ALL)
    private List<RoomEntity> rooms;
}
