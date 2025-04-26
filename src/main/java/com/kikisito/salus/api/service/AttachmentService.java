package com.kikisito.salus.api.service;

import com.kikisito.salus.api.entity.AttachmentEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
public class AttachmentService {
    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public AttachmentEntity saveAttachment(MultipartFile file) {
        // Almacenamos el archivo
        String filePath = fileStorageService.storeFile(file);

        // Creamos la entidad
        AttachmentEntity attachment = AttachmentEntity.builder()
                .name(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .filePath(filePath)
                .build();

        // Guardamos la entidad en la base de datos
        attachment = attachmentRepository.save(attachment);

        // Devolvemos la entidad que representa al archivo
        return attachment;
    }

    @Transactional(readOnly = true)
    public byte[] getAttachmentBytes(Integer id) {
        AttachmentEntity attachment = attachmentRepository.findById(id).orElseThrow(DataNotFoundException::attachmentNotFound);
        return fileStorageService.loadFileAsBytes(attachment.getFilePath());
    }

    @Transactional
    public void deleteAttachment(Integer id) {
        AttachmentEntity attachment = attachmentRepository.findById(id).orElseThrow(DataNotFoundException::attachmentNotFound);
        fileStorageService.deleteFile(attachment.getFilePath());
        attachmentRepository.delete(attachment);
    }

    @Transactional(readOnly = true)
    public boolean attachmentIdCanBeAccessedByUser(Integer attachmentId, UserEntity user) {
        // No está puesto si es dueño del archivo porque no contempla un doctor que sube un archivo y luego deja de ser doctor
        boolean isLinkedToMedicalTestAndUserIsPatient = attachmentRepository.existsByIdAndMedicalTest_Patient(attachmentId, user);
        boolean isLinkedToMedicalTestAndUserIsDoctor = user.getMedicalProfile() != null && attachmentRepository.existsByIdAndMedicalTest_Doctor(attachmentId, user.getMedicalProfile());

        return isLinkedToMedicalTestAndUserIsPatient || isLinkedToMedicalTestAndUserIsDoctor;
    }
}