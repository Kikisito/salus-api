package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.ReportDTO;
import com.kikisito.salus.api.dto.request.ReportRequest;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.*;
import com.kikisito.salus.api.type.ReportType;
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

@Service
@RequiredArgsConstructor
public class ReportService {
    @Autowired
    private final ReportRepository reportRepository;

    @Autowired
    private final AppointmentRepository appointmentRepository;

    @Autowired
    private final MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private MedicalCenterRepository medicalCenterRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<ReportDTO> getUserReports(Integer userId) {
        // Obtenemos el usuario que el usuario ha introducido
        UserEntity user = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);

        // Obtenemos todos los informes por su paciente
        List<ReportEntity> reports = reportRepository.findByPatient(user);

        // Devolvemos la lista de informes
        return reports.stream()
                .map(report -> modelMapper.map(report, ReportDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getAppointmentReports(Integer appointmentId) {
        // Obtenemos la cita que el usuario ha introducido
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow(DataNotFoundException::appointmentNotFound);

        // Obtenemos todos los informes por su cita
        List<ReportEntity> reports = reportRepository.findByAppointment(appointment);

        // Devolvemos la lista de informes
        return reports.stream()
                .map(report -> modelMapper.map(report, ReportDTO.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getPatientReportsWithDoctorOrItsSpecialties(Integer patientId, Integer doctorId) {
        UserEntity user = userRepository.findById(patientId).orElseThrow(DataNotFoundException::userNotFound);
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        List<ReportEntity> reports = reportRepository.findByPatientWithDoctorOrItsSpecialties(user, doctor, doctor.getSpecialties());

        return reports.stream()
                .map(report -> modelMapper.map(report, ReportDTO.class))
                .toList();
    }

    @Transactional
    public ReportDTO addReport(ReportRequest reportRequest) {
        // Si el informe está asociado a una cita, la recuperamos
        Optional<AppointmentEntity> appointment = reportRequest.getAppointment() != null ? appointmentRepository.findById(reportRequest.getAppointment()) : Optional.empty();

        // Obtenemos el médico asociado al informe
        MedicalProfileEntity medicalProfile = medicalProfileRepository.findById(reportRequest.getDoctor()).orElseThrow(DataNotFoundException::doctorNotFound);

        // Obtenemos el paciente asociado al informe
        UserEntity patient = userRepository.findById(reportRequest.getPatient()).orElseThrow(DataNotFoundException::userNotFound);

        // Obtenemos la especialidad asociada al informe
        SpecialtyEntity specialty = specialtyRepository.findById(reportRequest.getSpecialty()).orElseThrow(DataNotFoundException::specialtyNotFound);

        // Creamos el informe con las entidades asociadas
        ReportEntity report = ReportEntity.builder()
                .appointment(appointment.orElse(null))
                .doctor(medicalProfile)
                .patient(patient)
                .specialty(specialty)
                .build();

        // Mapeamos el resto de datos del informe
        modelMapper.map(reportRequest, report);

        // Guardamos el informe
        report = reportRepository.save(report);

        // Devolvemos el informe guardado
        return modelMapper.map(report, ReportDTO.class);
    }

    @Transactional(readOnly = true)
    public ReportDTO getReport(Integer reportId) {
        // Obtenemos el informe por su id
        ReportEntity report = reportRepository.findById(reportId).orElseThrow(DataNotFoundException::reportNotFound);

        // Devolvemos el informe
        return modelMapper.map(report, ReportDTO.class);
    }

    @Transactional(readOnly = true)
    public byte[] getReportPdf(Integer reportId) {
        // Obtenemos el informe por su id
        ReportEntity report = reportRepository.findById(reportId).orElseThrow(DataNotFoundException::reportNotFound);

        // Generamos el PDF a partir del informe
        String html = this.getTemplateHtml(report.getType());
        html = this.replacePlaceholders(html, report);
        byte[] pdf = this.generatePdfFromHtml(html);

        return pdf;
    }

    @Transactional(readOnly = true)
    public boolean canProfessionalAccessReport(Integer reportId, Integer doctorId) {
        ReportEntity report = reportRepository.findById(reportId).orElseThrow(DataNotFoundException::reportNotFound);
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        // Comprobamos si el informe está asociado al médico, a una cita cuyo doctor es el médico o a una especialidad del médico
        boolean isReportAssociatedToDoctor = report.getDoctor().equals(doctor);
        boolean isReportAppointmentAssociatedToDoctor = report.getAppointment() != null && report.getAppointment().getSlot().getDoctor().equals(doctor);
        boolean isReportSpecialtyAssociatedToDoctorSpecialties = report.getDoctor().getSpecialties().stream()
                .anyMatch(specialty -> doctor.getSpecialties().contains(specialty));

        return isReportAssociatedToDoctor || isReportAppointmentAssociatedToDoctor || isReportSpecialtyAssociatedToDoctorSpecialties;
    }

    @Transactional
    public ReportDTO updateReport(Integer reportId, ReportRequest reportRequest) {
        ReportEntity report = reportRepository.findById(reportId).orElseThrow(DataNotFoundException::reportNotFound);

        // Solo permitimos cambiar la descripción, diagnóstico, tratamiento y observaciones
        report.setDescription(reportRequest.getDescription());
        report.setDiagnosis(reportRequest.getDiagnosis());
        report.setTreatment(reportRequest.getTreatment());
        report.setObservations(reportRequest.getObservations());

        // Guardamos el informe
        report = reportRepository.save(report);

        // Devolvemos el informe guardado
        return modelMapper.map(report, ReportDTO.class);
    }

    @Transactional
    public void deleteReport(Integer reportId) {
        // Obtenemos el informe por su id
        ReportEntity report = reportRepository.findById(reportId).orElseThrow(DataNotFoundException::reportNotFound);

        // Eliminamos el informe
        reportRepository.delete(report);
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

    private String replacePlaceholders(String html, ReportEntity report) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Logo
        html = html.replace("${LOGO}", this.getLogoPath());

        // Datos del paciente
        html = html.replace("${PATIENT_NAME}", report.getPatient().getNombre());
        html = html.replace("${PATIENT_SURNAME}", report.getPatient().getApellidos());
        html = html.replace("${PATIENT_ID_CARD}", report.getPatient().getNif());
        html = html.replace("${PATIENT_BIRTH_DATE}", report.getPatient().getFechaNacimiento().format(dateFormatter));

        // Datos de la consulta
        html = html.replace("${APPOINTMENT_DATE}", report.getAppointment() != null ? report.getAppointment().getSlot().getDate().format(dateFormatter) + " " + report.getAppointment().getSlot().getStartTime().format(timeFormatter) : "Sin consulta asociada");

        // Datos del informe
        html = html.replace("${DIAGNOSIS}", report.getDiagnosis());
        html = html.replace("${TREATMENT}", report.getTreatment());
        html = html.replace("${OBSERVATIONS}", report.getObservations());

        // Datos del centro médico
        MedicalCenterEntity medicalCenter = report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter() : medicalCenterRepository.findFirst().orElseThrow(DataNotFoundException::medicalCenterNotFound);
        html = html.replace("${MEDICAL_CENTER_NAME}", report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter().getName() : medicalCenter.getName());
        html = html.replace("${MEDICAL_CENTER_ADDRESS_LINE_1}", report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter().getAddressLine1() : medicalCenter.getAddressLine1());
        html = html.replace("${MEDICAL_CENTER_ADDRESS_LINE_2}", report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter().getAddressLine2() : medicalCenter.getAddressLine2());
        html = html.replace("${MEDICAL_CENTER_ZIP_CODE}", report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter().getZipCode() : medicalCenter.getZipCode());
        html = html.replace("${MEDICAL_CENTER_LOCALITY}", report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter().getLocality() : medicalCenter.getLocality());
        html = html.replace("${MEDICAL_CENTER_MUNICIPALITY}", report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter().getMunicipality() : medicalCenter.getMunicipality());
        html = html.replace("${MEDICAL_CENTER_PROVINCE}", report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter().getProvince() : medicalCenter.getProvince());
        html = html.replace("${MEDICAL_CENTER_COUNTRY}", report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter().getCountry() : medicalCenter.getCountry());
        html = html.replace("${MEDICAL_CENTER_PHONE}", report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter().getPhone() : medicalCenter.getPhone());
        html = html.replace("${MEDICAL_CENTER_EMAIL}", report.getAppointment() != null ? report.getAppointment().getSlot().getRoom().getMedicalCenter().getEmail() : medicalCenter.getEmail());

        // Datos del médico
        html = html.replace("${DOCTOR_NAME}", report.getDoctor().getUser().getNombre());
        html = html.replace("${DOCTOR_SURNAME}", report.getDoctor().getUser().getApellidos());
        html = html.replace("${DOCTOR_SPECIALTY}", report.getDoctor().getSpecialties().stream().map(SpecialtyEntity::getName).reduce((s1, s2) -> s1 + ", " + s2).orElse(""));
        html = html.replace("${DOCTOR_LICENSE}", report.getDoctor().getLicense());
        return html;
    }

    private String getTemplateHtml(ReportType reportType) {
        try {
            InputStream inputStream = new ClassPathResource("pdf-templates/" + reportType.name() + "_REPORT_TEMPLATE.html").getInputStream();
            String html = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            return html;
        } catch (IOException ex) {
            throw new RuntimeException("Error loading HTML template", ex);
        }
    }

    @Transactional(readOnly = true)
    public UserEntity getReportPatient(Integer reportId) {
        ReportEntity report = reportRepository.findById(reportId).orElseThrow(DataNotFoundException::reportNotFound);
        return report.getPatient();
    }
}