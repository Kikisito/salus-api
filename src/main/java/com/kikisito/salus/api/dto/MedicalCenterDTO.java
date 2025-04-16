package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MedicalCenterDTO {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String zipCode;
    private String country;
    private String province;
    private String municipality;
    private String locality;
    //private List<RoomDTO> rooms;
}
