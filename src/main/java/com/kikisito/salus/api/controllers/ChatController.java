package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.ChatDTO;
import com.kikisito.salus.api.dto.ChatMessageDTO;
import com.kikisito.salus.api.dto.request.ChatMessageRequest;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/chats")
@RequiredArgsConstructor
@CrossOrigin
public class ChatController {
    @Autowired
    private final ChatService chatService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAuthority('USER') and #patientId == authentication.principal.id")
    public ResponseEntity<List<ChatDTO>> getPatientChats(@PathVariable Integer patientId) {
        return ResponseEntity.ok(chatService.getPatientChats(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAuthority('PROFESSIONAL') and @medicalProfileService.getAsUser(#doctorId).id == authentication.principal.id")
    public ResponseEntity<List<ChatDTO>> getDoctorChats(@PathVariable Integer doctorId) {
        return ResponseEntity.ok(chatService.getDoctorChats(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/patient/{patientId}/info")
    @PreAuthorize("""
            (hasAuthority('USER') and #patientId == authentication.principal.id) or
            (hasAuthority('PROFESSIONAL') and @medicalProfileService.getAsUser(#doctorId).id == authentication.principal.id)
            """)
    public ResponseEntity<ChatDTO> getChatInfo(@PathVariable Integer doctorId, @PathVariable Integer patientId, @AuthenticationPrincipal UserEntity userRequest) {
        return ResponseEntity.ok(chatService.getChatInfo(doctorId, patientId, userRequest));
    }

    @GetMapping("/doctor/{doctorId}/patient/{patientId}")
    @PreAuthorize("""
            (hasAuthority('USER') and #patientId == authentication.principal.id) or
            (hasAuthority('PROFESSIONAL') and @medicalProfileService.getAsUser(#doctorId).id == authentication.principal.id)
            """)
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@PathVariable Integer doctorId, @PathVariable Integer patientId, @AuthenticationPrincipal UserEntity userRequest) {
        return ResponseEntity.ok(chatService.getChatMessages(doctorId, patientId, userRequest));
    }

    @PostMapping("/doctor/{doctorId}/patient/{patientId}")
    @PreAuthorize("""
            (hasAuthority('USER') and #patientId == authentication.principal.id) or
            (hasAuthority('PROFESSIONAL') and @medicalProfileService.getAsUser(#doctorId).id == authentication.principal.id)
            """)
    public ResponseEntity<ChatMessageDTO> sendMessage(@PathVariable Integer doctorId, @PathVariable Integer patientId, @RequestBody @Valid ChatMessageRequest chatMessageRequest, @AuthenticationPrincipal UserEntity sender) {
        return ResponseEntity.ok(chatService.sendMessage(doctorId, patientId, chatMessageRequest, sender));
    }
}