package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.DireccionDTO;
import com.kikisito.salus.api.dto.UserDTO;
import com.kikisito.salus.api.dto.request.CreateUserRequest;
import com.kikisito.salus.api.dto.request.RegisterRequest;
import com.kikisito.salus.api.dto.request.RestrictUserRequest;
import com.kikisito.salus.api.dto.response.UsersListResponse;
import com.kikisito.salus.api.embeddable.DireccionEmbeddable;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.MedicalProfileRepository;
import com.kikisito.salus.api.repository.UserRepository;
import com.kikisito.salus.api.type.AccountStatusType;
import com.kikisito.salus.api.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    @Value("${application.name}")
    private String appName;

    @Value("${application.host}")
    private String host;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final AuthService authService;

    @Autowired
    private final MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private final EmailingService emailingService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    private final static String WELCOME_EMAIL_NO_PASSWORD_TEXT_SUBJECT = "Te damos la bienvenida";

    @Transactional(readOnly = true)
    public UsersListResponse getAllUsers(Integer page, Integer limit) {
        Page<UserEntity> userEntities = userRepository.findAll(PageRequest.of(page, limit));
        List<UserDTO> usersDTO = userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDTO.class))
                .toList();

        return UsersListResponse.builder()
                .count(userRepository.count())
                .users(usersDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public UsersListResponse getDoctorPatients(Integer doctorId, Integer page, Integer limit) {
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        Page<UserEntity> userEntities = userRepository.findDoctorPatients(doctor, PageRequest.of(page, limit));
        List<UserDTO> usersDTO = userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDTO.class))
                .toList();

        return UsersListResponse.builder()
                .count(userRepository.countDoctorPatients(doctor))
                .users(usersDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public UsersListResponse getDoctorPatientsBySearch(Integer doctorId, String search, Integer page, Integer limit) {
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        Page<UserEntity> userEntities = userRepository.searchDoctorPatients(doctor, search, PageRequest.of(page, limit));
        List<UserDTO> usersDTO = userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDTO.class))
                .toList();

        return UsersListResponse.builder()
                .count(userRepository.searchDoctorPatientsCount(doctor, search))
                .users(usersDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public UsersListResponse searchUsers(String search, Integer page, Integer limit) {
        Page<UserEntity> userEntities = userRepository.searchUsers(search, PageRequest.of(page, limit));
        List<UserDTO> usersDTO = userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDTO.class))
                .toList();

        return UsersListResponse.builder()
                .count(userRepository.searchUsersCount(search))
                .users(usersDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public UserDTO getUserProfile(Integer id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(DataNotFoundException::userNotFound);
        return this.getUserProfile(userEntity);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserProfile(UserEntity userEntity) {
        // Se mapea la dirección a un DTO, si existe
        DireccionDTO direccionDTO = (userEntity.getDireccion() != null)
                ? modelMapper.map(userEntity.getDireccion(), DireccionDTO.class)
                : null;

        // Se mapea el usuario a un DTO y se le asigna la dirección para devolverlo como respuesta
        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
        userDTO.setDireccion(direccionDTO);
        return userDTO;
    }

    @Transactional
    public UserDTO updateProfile(int userIdTarget, UserDTO userDTO) {
        // Se busca el usuario por ID
        UserEntity userEntity = userRepository.findById(userIdTarget).orElseThrow(DataNotFoundException::userNotFound);
        return this.updateProfile(userEntity, userDTO);
    }

    @Transactional
    public UserDTO updateProfile(UserEntity userEntity, UserDTO userDTO) {
        // Se comprueba si el email o el NIF ya están registrados por *OTRO* usuario
        UserEntity existingUser = userRepository.findByEmail(userDTO.getEmail()).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userEntity.getId())) {
            throw ConflictException.emailIsRegistered();
        }

        existingUser = userRepository.findByNif(userDTO.getNif()).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userEntity.getId())) {
            throw ConflictException.nifIsRegistered();
        }

        // Si el email, que estaba verificado, se ha cambiado, se vuelve a marcar como cuenta no verificada
        if (!userEntity.getEmail().equals(userDTO.getEmail()) && userEntity.getAccountStatusType().equals(AccountStatusType.VERIFIED)) {
            userEntity.setAccountStatusType(AccountStatusType.NOT_VERIFIED);
        }

        // Se mapea el DTO a la entidad y se guarda
        userEntity.setNombre(userDTO.getNombre());
        userEntity.setApellidos(userDTO.getApellidos());
        userEntity.setNif(userDTO.getNif());
        userEntity.setSexo(userDTO.getSexo());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setTelefono(userDTO.getTelefono());
        userEntity.setFechaNacimiento(userDTO.getFechaNacimiento());
        userEntity = userRepository.save(userEntity);

        // Se mapea el usuario actualizado a un DTO y se devuelve
        return modelMapper.map(userEntity, UserDTO.class);
    }

    @Transactional
    public UserDTO updateAddress(int userId, DireccionDTO direccionDTO) {
        // Se busca el usuario por ID
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        return updateAddress(userEntity, direccionDTO);
    }

    @Transactional
    public UserDTO updateAddress(UserEntity userEntity, DireccionDTO direccionDTO) {
        // Se mapea la dirección a un objeto "embeddable", se actualiza el usuario y se guarda
        DireccionEmbeddable direccionEmbeddable = modelMapper.map(direccionDTO, DireccionEmbeddable.class);
        userEntity.setDireccion(direccionEmbeddable);
        userEntity = userRepository.save(userEntity);

        // Se mapea el usuario actualizado a un DTO y se le asigna la dirección para devolverlo como respuesta
        UserDTO userDTOUpdated = modelMapper.map(userEntity, UserDTO.class);
        userDTOUpdated.setDireccion(direccionDTO);
        return userDTOUpdated;
    }

    @Transactional
    public UserDTO restrictUser(int userId, RestrictUserRequest request) {
        // Se busca el usuario por ID
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);

        // Se restringe o se des-restringe el usuario
        userEntity.setRestricted(request.isRestrict());
        userEntity = userRepository.save(userEntity);

        return modelMapper.map(userEntity, UserDTO.class);
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // Comprobaciones previas (email/dni existentes)
        if(userRepository.existsByEmail(request.getEmail())) {
            throw ConflictException.emailIsRegistered();
        }

        if(userRepository.existsByNif(request.getNif())) {
            throw ConflictException.nifIsRegistered();
        }

        // Creamos el usuario y la dirección
        List<RoleType> roleTypeList = Collections.singletonList(RoleType.USER);

        // La dirección es opcional, pero si se ha proporcionado la guardamos
        DireccionEmbeddable direccionEmbeddable = DireccionEmbeddable.builder()
                .lineaDireccion1(request.getDireccion().getLineaDireccion1())
                .lineaDireccion2(request.getDireccion().getLineaDireccion2())
                .codigoPostal(request.getDireccion().getCodigoPostal())
                .pais(request.getDireccion().getPais())
                .provincia(request.getDireccion().getProvincia())
                .municipio(request.getDireccion().getMunicipio())
                .localidad(request.getDireccion().getLocalidad())
                .build();

        UserEntity userEntity = UserEntity.builder()
                .nif(request.getNif())
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .sexo(request.getSexo())
                .email(request.getEmail())
                .password(passwordEncoder.encode(this.generateRandomPassword(32))) // Al crear la cuenta un tercero, la contraseña será aleatoria y el usuario deberá resetearla
                .telefono(request.getTelefono())
                .fechaNacimiento(request.getFechaNacimiento())
                .direccion(direccionEmbeddable)
                .accountStatusType(AccountStatusType.NOT_VERIFIED)
                .rolesList(roleTypeList)
                .build();

        UserEntity savedUser = userRepository.save(userEntity);

        // Creamos un token para resetear la contraseña del usuario
        String passwordResetToken = authService.createPasswordResetToken(savedUser).getToken();

        // Enviamos el email de reseteo de contraseña
        Map<String, Object> variables = new HashMap<>();
        variables.put("appName", appName);
        variables.put("user", userEntity);
        variables.put("host", host);
        variables.put("passwordResetToken", passwordResetToken);

        emailingService.sendTemplateEmail(
                savedUser.getEmail(),
                WELCOME_EMAIL_NO_PASSWORD_TEXT_SUBJECT,
                "welcome-email-with-password-reset",
                variables
        );

        return modelMapper.map(savedUser, UserDTO.class);
    }

    private String generateRandomPassword(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}