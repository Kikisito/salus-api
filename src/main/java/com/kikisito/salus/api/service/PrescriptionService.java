package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.PrescriptionDTO;
import com.kikisito.salus.api.dto.request.MedicationRequest;
import com.kikisito.salus.api.dto.request.PrescriptionRequest;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
    @Autowired
    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    private final MedicationRepository medicationRepository;

    @Autowired
    private final AppointmentRepository appointmentRepository;

    @Autowired
    private final MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final SpecialtyRepository specialtyRepository;

    @Autowired
    private final MedicalCenterRepository medicalCenterRepository;

    @Autowired
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getAppointmentPrescriptions(Integer appointmentId) {
        AppointmentEntity appointmentEntity = appointmentRepository.findById(appointmentId).orElseThrow(DataNotFoundException::appointmentNotFound);

        List<PrescriptionEntity> prescriptions = prescriptionRepository.findAllByAppointment(appointmentEntity);

        return prescriptions.stream()
                .map(prescription -> modelMapper.map(prescription, PrescriptionDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getDoctorPrescriptions(Integer doctorId) {
        MedicalProfileEntity medicalProfile = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);
        List<PrescriptionEntity> prescriptions = prescriptionRepository.findAllByDoctor(medicalProfile);
        return prescriptions.stream()
                .map(prescription -> modelMapper.map(prescription, PrescriptionDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPatientPrescriptions(Integer userId) {
        UserEntity patient = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        List<PrescriptionEntity> prescriptions = prescriptionRepository.findAllByPatient(patient);
        return prescriptions.stream()
                .map(prescription -> modelMapper.map(prescription, PrescriptionDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] getPrescriptionPdf(Integer prescriptionId) {
        PrescriptionEntity prescription = prescriptionRepository.findById(prescriptionId).orElseThrow(DataNotFoundException::prescriptionNotFound);

        String html = this.getTemplateHtml();
        String filledHtml = this.replacePrescriptionPlaceholders(html, prescription);

        return this.generatePdfFromHtml(filledHtml);
    }


    @Transactional
    public PrescriptionDTO addPrescription(PrescriptionRequest request) {
        // Doctor
        MedicalProfileEntity doctor = medicalProfileRepository.findById(request.getDoctor()).orElseThrow(DataNotFoundException::doctorNotFound);

        // Paciente
        UserEntity patient = userRepository.findById(request.getPatient()).orElseThrow(DataNotFoundException::userNotFound);

        // Especialidad
        SpecialtyEntity specialty = specialtyRepository.findById(request.getSpecialty()).orElseThrow(DataNotFoundException::specialtyNotFound);

        // Cita
        Optional<AppointmentEntity> appointmentOptional = appointmentRepository.findById(request.getAppointment());

        // La medicación se hace después de la prescripción
        PrescriptionEntity initialPrescription = PrescriptionEntity.builder()
                .doctor(doctor)
                .patient(patient)
                .specialty(specialty)
                .appointment(appointmentOptional.orElse(null))
                .build();

        PrescriptionEntity savedPrescription = prescriptionRepository.save(initialPrescription);

        // Creamos y guardamos en la DB las medicaciones
        List<MedicationEntity> medications = request.getMedications().stream()
                .map(medicationDTO -> {
                    MedicationEntity medication = MedicationEntity.builder()
                            .name(medicationDTO.getName())
                            .dosage(medicationDTO.getDosage())
                            .frequency(medicationDTO.getFrequency())
                            .startDate(medicationDTO.getStartDate())
                            .endDate(medicationDTO.getEndDate())
                            .instructions(medicationDTO.getInstructions())
                            .prescription(savedPrescription) // Conexión a la receta
                            .build();
                    return medicationRepository.save(medication);
                }
                ).collect(Collectors.toList());

        // Asignamos la lista de medicaciones a la receta
        savedPrescription.setMedications(medications);

        // Guardamos la receta con la lista de medicaciones
        PrescriptionEntity prescription = prescriptionRepository.save(savedPrescription);

        // Convertimos la receta a DTO para devolverla
        return modelMapper.map(prescription, PrescriptionDTO.class);
    }

    @Transactional
    public PrescriptionDTO updatePrescription(Integer prescriptionId, PrescriptionRequest request) {
        // Obtenemos la receta de la base de datos
        PrescriptionEntity prescription = prescriptionRepository.findById(prescriptionId).orElseThrow(DataNotFoundException::prescriptionNotFound);

        // Medicamentos existentes en la receta en la base de datos
        List<MedicationEntity> existingMedications = prescription.getMedications();

        // Medicamentos de la petición
        List<MedicationRequest> requestMedications = request.getMedications();

        // Borramos los medicamentos de la base de datos que no están en la petición
        List<MedicationEntity> deletingMedications = existingMedications.stream()
                .filter(medication -> requestMedications.stream().noneMatch(requestMedication -> requestMedication.getId() != null && requestMedication.getId().equals(medication.getId())))
                .toList();

        for(MedicationEntity medication : deletingMedications) {
            prescription.getMedications().remove(medication);
        }

        prescription = prescriptionRepository.save(prescription);

        // Ahora añadimos los nuevos medicamentos
        PrescriptionEntity finalPrescription = prescription;
        List<MedicationEntity> newMedications = requestMedications.stream().filter(med -> med.getId() == null)
                .map(med -> MedicationEntity.builder()
                        .name(med.getName())
                        .dosage(med.getDosage())
                        .frequency(med.getFrequency())
                        .startDate(med.getStartDate())
                        .endDate(med.getEndDate())
                        .instructions(med.getInstructions())
                        .prescription(finalPrescription) // Conexión a la receta
                        .build()
                ).collect(Collectors.toList());

        // Añadimos los nuevos medicamentos a la receta
        prescription.addMedications(newMedications);

        // Guardamos la receta con la lista de medicinas nueva
        prescriptionRepository.save(prescription);

        // Devolvemos la receta actualizada
        return modelMapper.map(prescription, PrescriptionDTO.class);
    }

    @Transactional
    public void deletePrescription(Integer prescriptionId) {
        PrescriptionEntity prescription = prescriptionRepository.findById(prescriptionId).orElseThrow(DataNotFoundException::prescriptionNotFound);
        prescriptionRepository.delete(prescription);
    }

    @Transactional(readOnly = true)
    public boolean canProfessionalAccessPrescription(Integer prescriptionId, Integer professionalId) {
        PrescriptionEntity prescription = prescriptionRepository.findById(prescriptionId).orElseThrow(DataNotFoundException::prescriptionNotFound);
        MedicalProfileEntity doctor = medicalProfileRepository.findById(professionalId).orElseThrow(DataNotFoundException::doctorNotFound);

        // Comprobamos si la receta está asociado al médico, a una cita cuyo doctor es el médico o a una especialidad del médico
        boolean isPrescriptionAssociatedToDoctor = prescription.getDoctor().equals(doctor);
        boolean isPrescriptionAppointmentAssociatedToDoctor = prescription.getAppointment() != null && prescription.getAppointment().getSlot().getDoctor().equals(doctor);
        boolean isPrescriptionSpecialtyAssociatedToDoctorSpecialties = prescription.getDoctor().getSpecialties().stream()
                .anyMatch(specialty -> doctor.getSpecialties().contains(specialty));

        return isPrescriptionAssociatedToDoctor || isPrescriptionAppointmentAssociatedToDoctor || isPrescriptionSpecialtyAssociatedToDoctorSpecialties;
    }

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

    private String replacePrescriptionPlaceholders(String html, PrescriptionEntity prescription) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        html = html.replace("${LOGO}", getLogoPath());

        // Paciente
        html = html.replace("${PATIENT_NAME}", prescription.getPatient().getNombre());
        html = html.replace("${PATIENT_SURNAME}", prescription.getPatient().getApellidos());
        html = html.replace("${PATIENT_ID_CARD}", prescription.getPatient().getNif());
        html = html.replace("${PATIENT_BIRTH_DATE}", prescription.getPatient().getFechaNacimiento().format(dateFormatter));

        // Fecha
        html = html.replace("${PRESCRIPTION_DATE}", prescription.getCreatedAt().format(dateTimeFormatter));

        // Datos del centro médico
        MedicalCenterEntity medicalCenter = prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter() : medicalCenterRepository.findFirst().orElseThrow(DataNotFoundException::medicalCenterNotFound);
        html = html.replace("${MEDICAL_CENTER_NAME}", prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter().getName() : medicalCenter.getName());
        html = html.replace("${MEDICAL_CENTER_ADDRESS_LINE_1}", prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter().getAddressLine1() : medicalCenter.getAddressLine1());
        html = html.replace("${MEDICAL_CENTER_ADDRESS_LINE_2}", prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter().getAddressLine2() : medicalCenter.getAddressLine2());
        html = html.replace("${MEDICAL_CENTER_ZIP_CODE}", prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter().getZipCode() : medicalCenter.getZipCode());
        html = html.replace("${MEDICAL_CENTER_LOCALITY}", prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter().getLocality() : medicalCenter.getLocality());
        html = html.replace("${MEDICAL_CENTER_MUNICIPALITY}", prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter().getMunicipality() : medicalCenter.getMunicipality());
        html = html.replace("${MEDICAL_CENTER_PROVINCE}", prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter().getProvince() : medicalCenter.getProvince());
        html = html.replace("${MEDICAL_CENTER_COUNTRY}", prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter().getCountry() : medicalCenter.getCountry());
        html = html.replace("${MEDICAL_CENTER_PHONE}", prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter().getPhone() : medicalCenter.getPhone());
        html = html.replace("${MEDICAL_CENTER_EMAIL}", prescription.getAppointment() != null ? prescription.getAppointment().getSlot().getRoom().getMedicalCenter().getEmail() : medicalCenter.getEmail());


        // Médico
        html = html.replace("${DOCTOR_NAME}", prescription.getDoctor().getUser().getNombre());
        html = html.replace("${DOCTOR_SURNAME}", prescription.getDoctor().getUser().getApellidos());
        html = html.replace("${DOCTOR_SPECIALTY}", prescription.getSpecialty().getName());
        html = html.replace("${DOCTOR_LICENSE}", prescription.getDoctor().getLicense());

        // Medicación
        StringBuilder medsBuilder = new StringBuilder();
        for (MedicationEntity med : prescription.getMedications()) {
            String instructions = med.getInstructions() != null ? med.getInstructions() : "";
            medsBuilder.append("<tr>")
                    .append("<td>").append(med.getName()).append("</td>")
                    .append("<td>").append(med.getDosage()).append("</td>")
                    .append("<td>").append(med.getFrequency()).append("</td>")
                    .append("<td>").append(med.getStartDate()).append("</td>")
                    .append("<td>").append(med.getEndDate()).append("</td>")
                    .append("<td>").append(instructions).append("</td>")
                    .append("</tr>");
        }
        html = html.replace("${MEDICATIONS_ROWS}", medsBuilder.toString());

        return html;
    }

    private String getTemplateHtml() {
        try {
            InputStream inputStream = new ClassPathResource("pdf-templates/PRESCRIPTION_TEMPLATE.html").getInputStream();
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading HTML template", ex);
        }
    }
}