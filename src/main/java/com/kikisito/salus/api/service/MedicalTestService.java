package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.MedicalTestDTO;
import com.kikisito.salus.api.dto.request.MedicalTestRequest;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicalTestService {
    @Autowired
    private final MedicalTestRepository medicalTestRepository;

    @Autowired
    private final AttachmentService attachmentService;

    @Autowired
    private final MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final SpecialtyRepository specialtyRepository;

    @Autowired
    private final AppointmentRepository appointmentRepository;

    @Autowired
    private final AttachmentRepository attachmentRepository;

    @Autowired
    private final MedicalCenterRepository medicalCenterRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public byte[] getMedicalTestPdf(Integer medicalTestId) {
        // Obtenemos el informe por su id
        MedicalTestEntity report = medicalTestRepository.findById(medicalTestId).orElseThrow(DataNotFoundException::medicalTestNotFound);

        // Generamos el PDF a partir del informe
        String html = this.getTemplateHtml();
        html = this.replacePlaceholders(html, report);
        byte[] pdf = this.generatePdfFromHtml(html);

        return pdf;
    }

    @Transactional
    public MedicalTestDTO addMedicalTest(MedicalTestRequest request, List<MultipartFile> files) {
        // Obtenemos las entidades con las que la prueba está relacionada
        MedicalProfileEntity doctor = medicalProfileRepository.findById(request.getDoctor()).orElseThrow(DataNotFoundException::doctorNotFound);
        UserEntity patient = userRepository.findById(request.getPatient()).orElseThrow(DataNotFoundException::userNotFound);
        SpecialtyEntity specialty = specialtyRepository.findById(request.getSpecialty()).orElseThrow(DataNotFoundException::specialtyNotFound);
        Optional<AppointmentEntity> appointmentOptional = appointmentRepository.findById(request.getAppointment());

        // Guardamos la prueba médica
        MedicalTestEntity medicalTestEntity = modelMapper.map(request, MedicalTestEntity.class);
        medicalTestEntity.setDoctor(doctor);
        medicalTestEntity.setPatient(patient);
        medicalTestEntity.setSpecialty(specialty);
        appointmentOptional.ifPresent(medicalTestEntity::setAppointment);

        // Guardamos la prueba médica
        medicalTestEntity = medicalTestRepository.save(medicalTestEntity);

        // Guardamos los archivos adjuntos. Los guardamos después de guardar la prueba médica para minimizar errores y
        // que no se guarden archivos adjuntos sin relación a entidades
        List<AttachmentEntity> attachments = files == null ? new ArrayList<>() : files.stream()
                .map(attachmentService::saveAttachment)
                .toList();

        // Asignamos la prueba médica a los archivos adjuntos
        for(AttachmentEntity attachment : attachments) {
            attachment.setMedicalTest(medicalTestEntity);
            attachmentRepository.save(attachment);
        }

        // Mapeamos la entidad a DTO y la devolvemos
        medicalTestEntity.setAttachments(attachments); // Asignamos los archivos adjuntos a la prueba médica para que se mapeen
        return modelMapper.map(medicalTestEntity, MedicalTestDTO.class);
    }

    @Transactional
    public void deleteMedicalTest(Integer id) {
        // Obtenemos la prueba médica
        MedicalTestEntity medicalTestEntity = medicalTestRepository.findById(id).orElseThrow(DataNotFoundException::medicalTestNotFound);

        // Obtenemos los archivos adjuntos
        List<AttachmentEntity> attachments = attachmentRepository.findByMedicalTest(medicalTestEntity);

        // Eliminamos los archivos adjuntos
        for(AttachmentEntity attachment : attachments) {
            attachmentService.deleteAttachment(attachment.getId());
        }
        // Eliminamos la prueba médica
        medicalTestRepository.delete(medicalTestEntity);
    }

    @Transactional(readOnly = true)
    public boolean isDoctorResponsibleOfMedicalTest(Integer medicalTestId, Integer doctorId) {
        return medicalTestRepository.existsByIdAndDoctor_Id(medicalTestId, doctorId);
    }

    // PDF

    private byte[] generatePdfFromHtml(String templateHtml) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(templateHtml, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Error generating PDF", ex);
        }
    }

    private String getLogoPath() {
        try {
            ClassPathResource logo = new ClassPathResource("pdf-templates/logo.png");
            return "file:///" + logo.getFile().getAbsolutePath();
        } catch (IOException ex) {
            throw new RuntimeException("Error loading logo", ex);
        }
    }

    private String replacePlaceholders(String html, MedicalTestEntity medicalTest) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Logo
        html = html.replace("${LOGO}", this.getLogoPath());

        // Datos del paciente
        html = html.replace("${PATIENT_NAME}", medicalTest.getPatient().getNombre());
        html = html.replace("${PATIENT_SURNAME}", medicalTest.getPatient().getApellidos());
        html = html.replace("${PATIENT_ID_CARD}", medicalTest.getPatient().getNif());
        html = html.replace("${PATIENT_BIRTH_DATE}", medicalTest.getPatient().getFechaNacimiento().format(dateFormatter));

        // Datos de la prueba médica
        html = html.replace("${REQUESTED_AT}", medicalTest.getRequestedAt() != null ? medicalTest.getRequestedAt().format(dateFormatter) : "Sin fecha");
        html = html.replace("${SCHEDULED_AT}", medicalTest.getScheduledAt() != null ? medicalTest.getScheduledAt().format(dateFormatter) : "Sin fecha");
        html = html.replace("${COMPLETED_AT}", medicalTest.getCompletedAt() != null ? medicalTest.getCompletedAt().format(dateFormatter) : "Sin fecha");

        html = html.replace("${TEST_NAME}", medicalTest.getName());
        html = html.replace("${SPECIALTY_NAME}", medicalTest.getSpecialty() != null ? medicalTest.getSpecialty().getName() : "Sin especialidad");
        html = html.replace("${TEST_DESCRIPTION}", medicalTest.getDescription());
        html = html.replace("${TEST_RESULT}", medicalTest.getResult() != null ? medicalTest.getResult() : "Sin resultado");
        html = html.replace("${TEST_OBSERVATIONS}", medicalTest.getObservations() != null ? medicalTest.getObservations() : "Sin observaciones");

        // Datos del centro médico
        MedicalCenterEntity medicalCenter = medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter() : medicalCenterRepository.findFirst().orElseThrow(DataNotFoundException::medicalCenterNotFound);
        html = html.replace("${MEDICAL_CENTER_NAME}", medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter().getName() : medicalCenter.getName());
        html = html.replace("${MEDICAL_CENTER_ADDRESS_LINE_1}", medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter().getAddressLine1() : medicalCenter.getAddressLine1());
        html = html.replace("${MEDICAL_CENTER_ADDRESS_LINE_2}", medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter().getAddressLine2() : medicalCenter.getAddressLine2());
        html = html.replace("${MEDICAL_CENTER_ZIP_CODE}", medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter().getZipCode() : medicalCenter.getZipCode());
        html = html.replace("${MEDICAL_CENTER_LOCALITY}", medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter().getLocality() : medicalCenter.getLocality());
        html = html.replace("${MEDICAL_CENTER_MUNICIPALITY}", medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter().getMunicipality() : medicalCenter.getMunicipality());
        html = html.replace("${MEDICAL_CENTER_PROVINCE}", medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter().getProvince() : medicalCenter.getProvince());
        html = html.replace("${MEDICAL_CENTER_COUNTRY}", medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter().getCountry() : medicalCenter.getCountry());
        html = html.replace("${MEDICAL_CENTER_PHONE}", medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter().getPhone() : medicalCenter.getPhone());
        html = html.replace("${MEDICAL_CENTER_EMAIL}", medicalTest.getAppointment() != null ? medicalTest.getAppointment().getSlot().getRoom().getMedicalCenter().getEmail() : medicalCenter.getEmail());

        // Datos del médico
        html = html.replace("${DOCTOR_NAME}", medicalTest.getDoctor().getUser().getNombre());
        html = html.replace("${DOCTOR_SURNAME}", medicalTest.getDoctor().getUser().getApellidos());
        html = html.replace("${DOCTOR_SPECIALTY}", medicalTest.getDoctor().getSpecialties().stream().map(SpecialtyEntity::getName).reduce((s1, s2) -> s1 + ", " + s2).orElse(""));
        html = html.replace("${DOCTOR_LICENSE}", medicalTest.getDoctor().getLicense());
        return html;
    }

    private String getTemplateHtml() {
        try {
            InputStream inputStream = new ClassPathResource("pdf-templates/MEDICAL_TEST_TEMPLATE.html").getInputStream();
            String html = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            return html;
        } catch (IOException ex) {
            throw new RuntimeException("Error loading HTML template", ex);
        }
    }
}