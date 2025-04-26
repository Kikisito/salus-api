package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/attachments")
@RequiredArgsConstructor
@CrossOrigin
public class AttachmentsController {
    @Autowired
    private final AttachmentService attachmentService;

    @GetMapping("/{attachmentId}/download")
    @PreAuthorize("hasAuthority('ADMIN') or @attachmentService.attachmentIdCanBeAccessedByUser(#attachmentId, authentication.principal)")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Integer attachmentId) {
        return ResponseEntity.ok(attachmentService.getAttachmentBytes(attachmentId));
    }
}
