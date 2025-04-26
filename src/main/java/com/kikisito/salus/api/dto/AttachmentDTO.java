package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AttachmentDTO {
    private Integer id;
    private String name;
    private String contentType;
    private Long size;
    private String filePath;
    private LocalDateTime uploadDate;
    private UserDTO uploadedBy;
}
