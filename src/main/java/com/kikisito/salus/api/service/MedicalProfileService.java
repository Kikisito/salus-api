package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.ReducedUserDTO;
import com.kikisito.salus.api.dto.SpecialtyDTO;
import com.kikisito.salus.api.dto.MedicalProfileDTO;
import com.kikisito.salus.api.dto.UserDTO;
import com.kikisito.salus.api.dto.request.AddDoctorSpecialtyRequest;
import com.kikisito.salus.api.dto.request.DoctorLicenseRequest;
import com.kikisito.salus.api.dto.response.DoctorsListResponse;
import com.kikisito.salus.api.entity.SpecialtyEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.AppointmentRepository;
import com.kikisito.salus.api.repository.SpecialtyRepository;
import com.kikisito.salus.api.repository.MedicalProfileRepository;
import com.kikisito.salus.api.repository.UserRepository;
import com.kikisito.salus.api.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalProfileService {
    @Autowired
    private final MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final SpecialtyRepository specialtyRepository;

    @Autowired
    private final AppointmentRepository appointmentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public DoctorsListResponse getMedicalProfiles(Integer page, Integer limit) {
        Page<MedicalProfileEntity> medicalProfiles = medicalProfileRepository.findAll(PageRequest.of(page, limit));

        List<MedicalProfileDTO> doctorsDTO = medicalProfiles.stream()
                .map(perfilMedicoEntity -> modelMapper.map(perfilMedicoEntity, MedicalProfileDTO.class))
                .toList();

        return DoctorsListResponse.builder()
                .count(medicalProfileRepository.count())
                .doctors(doctorsDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public DoctorsListResponse searchMedicalProfiles(String search, Integer page, Integer limit) {
        Page<MedicalProfileEntity> medicalProfiles = medicalProfileRepository.search(search, PageRequest.of(page, limit));

        List<MedicalProfileDTO> doctorsDTO = medicalProfiles.stream()
                .map(perfilMedicoEntity -> modelMapper.map(perfilMedicoEntity, MedicalProfileDTO.class))
                .toList();

        return DoctorsListResponse.builder()
                .count(medicalProfileRepository.searchCount(search))
                .doctors(doctorsDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public MedicalProfileDTO getMedicalProfileFromUserEntity(UserEntity userEntity) {
        MedicalProfileEntity medicalProfileEntity = medicalProfileRepository.findByUser(userEntity).orElseThrow(DataNotFoundException::doctorNotFound);
        return modelMapper.map(medicalProfileEntity, MedicalProfileDTO.class);
    }

    @Transactional(readOnly = true)
    public MedicalProfileDTO getMedicalProfile(Integer id) {
        MedicalProfileEntity medicalProfileEntity = medicalProfileRepository.findById(id).orElseThrow(DataNotFoundException::doctorNotFound);
        return modelMapper.map(medicalProfileEntity, MedicalProfileDTO.class);
    }

    @Transactional
    public MedicalProfileDTO createMedicalProfileFromUserEntity(DoctorLicenseRequest doctorLicenseRequest) {
        UserEntity userEntity = userRepository.findById(doctorLicenseRequest.getUserId()).orElseThrow(DataNotFoundException::userNotFound);
        String license = doctorLicenseRequest.getLicense();

        // Comprobamos que el usuario no sea un médico ya y que el número de colegiado no exista
        if(medicalProfileRepository.existsMedicoEntitiesByUser(userEntity)) {
            throw ConflictException.userAlreadyDoctor();
        } else if (medicalProfileRepository.existsMedicoEntitiesByLicense(license)) {
            throw ConflictException.licenseAlreadyExists();
        }

        // Creamos y guardamos el perfil médico
        MedicalProfileEntity medicalProfileEntity = MedicalProfileEntity.builder()
                .user(userEntity)
                .license(license)
                .build();
        medicalProfileEntity = medicalProfileRepository.save(medicalProfileEntity);

        // Le asignamos el rol PROFESSIONAL al usuario si no lo tiene
        if(!userEntity.getRolesList().contains(RoleType.PROFESSIONAL)) {
            userEntity.getRolesList().add(RoleType.PROFESSIONAL);
            userEntity = userRepository.save(userEntity);
        }

        // Devolvemos el perfil médico tras mapearlo a DTO con el usuario DTO
        MedicalProfileDTO medicalProfileDTO = modelMapper.map(medicalProfileEntity, MedicalProfileDTO.class);
        medicalProfileDTO.setUser(modelMapper.map(userEntity, ReducedUserDTO.class));
        return medicalProfileDTO;
    }

    @Transactional
    public MedicalProfileDTO changeLicense(Integer id, DoctorLicenseRequest doctorLicenseRequest) {
        MedicalProfileEntity medicalProfileEntity = medicalProfileRepository.findById(id).orElseThrow(DataNotFoundException::doctorNotFound);
        String license = doctorLicenseRequest.getLicense();

        // Comprobamos que el número de colegiado no exista
        if(medicalProfileRepository.existsMedicoEntitiesByLicense(license)) {
            throw ConflictException.licenseAlreadyExists();
        }

        // Cambiamos el número de colegiado y guardamos
        medicalProfileEntity.setLicense(license);
        medicalProfileEntity = medicalProfileRepository.save(medicalProfileEntity);

        return modelMapper.map(medicalProfileEntity, MedicalProfileDTO.class);
    }

    @Transactional(readOnly = true)
    public List<SpecialtyDTO> getSpecialties(Integer medicoId) {
        MedicalProfileEntity medicalProfileEntity = medicalProfileRepository.findById(medicoId).orElseThrow(DataNotFoundException::doctorNotFound);

        return medicalProfileEntity.getSpecialties().stream()
                .map(especialidad -> modelMapper.map(especialidad, SpecialtyDTO.class))
                .toList();
    }

    @Transactional
    public MedicalProfileDTO addSpecialtyToMedicalProfile(Integer doctorId, AddDoctorSpecialtyRequest addDoctorSpecialtyRequest) {
        MedicalProfileEntity medicalProfileEntity = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);
        SpecialtyEntity specialtyEntity = specialtyRepository.findById(addDoctorSpecialtyRequest.getSpecialtyId()).orElseThrow(DataNotFoundException::specialtyNotFound);

        // Comprobamos que el médico no tenga ya la especialidad
        if(medicalProfileEntity.getSpecialties().contains(specialtyEntity)) {
            throw ConflictException.doctorHasAlreadySpecialty();
        }

        // Añadimos la especialidad al médico y guardamos
        medicalProfileEntity.getSpecialties().add(specialtyEntity);
        medicalProfileRepository.save(medicalProfileEntity);

        return modelMapper.map(medicalProfileEntity, MedicalProfileDTO.class);
    }

    @Transactional
    public MedicalProfileDTO removeSpecialtyFromMedicalProfile(Integer medicoId, Integer especialidadId) {
        MedicalProfileEntity medicalProfileEntity = medicalProfileRepository.findById(medicoId).orElseThrow(DataNotFoundException::doctorNotFound);
        SpecialtyEntity specialtyEntity = specialtyRepository.findById(especialidadId).orElseThrow(DataNotFoundException::specialtyNotFound);

        medicalProfileEntity.getSpecialties().remove(specialtyEntity);
        medicalProfileRepository.save(medicalProfileEntity);

        return modelMapper.map(medicalProfileEntity, MedicalProfileDTO.class);
    }

    @Transactional
    public boolean deleteMedicalProfile(Integer id) {
        MedicalProfileEntity medicalProfile = medicalProfileRepository.findById(id).orElseThrow(DataNotFoundException::doctorNotFound);

        // Solo se permite eliminar un perfil médico si no tiene datos asociados como médico. Por lo general, comprobando citas debería ser suficiente
        if(appointmentRepository.existsBySlot_Doctor(medicalProfile)) {
            throw ConflictException.doctorHasMedicalDataLinked();
        }

        medicalProfileRepository.delete(medicalProfile);
        return true;
    }
}