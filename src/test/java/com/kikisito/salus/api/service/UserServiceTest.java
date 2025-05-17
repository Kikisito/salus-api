package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.DireccionDTO;
import com.kikisito.salus.api.dto.UserDTO;
import com.kikisito.salus.api.dto.request.CreateUserRequest;
import com.kikisito.salus.api.dto.request.RestrictUserRequest;
import com.kikisito.salus.api.dto.response.UsersListResponse;
import com.kikisito.salus.api.embeddable.DireccionEmbeddable;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.PasswordResetEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.MedicalProfileRepository;
import com.kikisito.salus.api.repository.UserRepository;
import com.kikisito.salus.api.type.AccountStatusType;
import com.kikisito.salus.api.type.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private MedicalProfileRepository medicalProfileRepository;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailingService emailingService;

    @Autowired
    private ModelMapper modelMapper;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    private UserEntity testUser;
    private UserDTO testUserDTO;
    private MedicalProfileEntity testMedicalProfile;
    private DireccionEmbeddable testDireccion;
    private Page<UserEntity> testPagedUsers;

    @BeforeEach
    void setUp() {
        testDireccion = DireccionEmbeddable.builder()
                .lineaDireccion1("Calle Test 123")
                .lineaDireccion2("Piso 4")
                .codigoPostal("03001")
                .pais("España")
                .provincia("Alicante")
                .municipio("Alicante")
                .localidad("Alicante")
                .build();
        DireccionDTO testDireccionDTO = modelMapper.map(testDireccion, DireccionDTO.class);

        testUser = UserEntity.builder()
                .id(1)
                .nombre("Juan")
                .apellidos("Jiménez")
                .email("juanji@salus.com")
                .nif("12345678A")
                .telefono("63819923")
                .fechaNacimiento(LocalDate.of(1993, 4, 12))
                .direccion(testDireccion)
                .rolesList(new ArrayList<>(List.of(RoleType.USER)))
                .accountStatusType(AccountStatusType.VERIFIED)
                .restricted(false)
                .loginAttempts(0)
                .password("password")
                .build();
        testUserDTO = modelMapper.map(testUser, UserDTO.class);
        testUserDTO.setDireccion(testDireccionDTO);

        testMedicalProfile = MedicalProfileEntity.builder()
                .id(1)
                .user(UserEntity.builder().id(2).build())
                .license("COLEG-TEST-1")
                .specialties(new ArrayList<>())
                .build();

        testPagedUsers = new PageImpl<>(Collections.singletonList(testUser));
    }

    @Test
    void C1_getAllUsers_should_return_one_user() {
        // Arrange
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(testPagedUsers);
        when(userRepository.count()).thenReturn(1L);

        // Act
        UsersListResponse response = userService.getAllUsers(0, 10);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getCount());
            assertEquals(1, response.getUsers().size());
            assertEquals(testUserDTO.getId(), response.getUsers().get(0).getId());
            assertEquals(testUserDTO.getNombre(), response.getUsers().get(0).getNombre());
            assertEquals(testUserDTO.getApellidos(), response.getUsers().get(0).getApellidos());
            assertEquals(testUserDTO.getEmail(), response.getUsers().get(0).getEmail());
        });

        verify(userRepository).findAll(any(PageRequest.class));
        verify(userRepository).count();
    }

    @Test
    void C2_getUserProfile_should_return_user_profile_when_id_is_1() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Act
        UserDTO response = assertDoesNotThrow(() -> userService.getUserProfile(1));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testUserDTO.getId(), response.getId());
            assertEquals(testUserDTO.getNombre(), response.getNombre());
            assertEquals(testUserDTO.getApellidos(), response.getApellidos());
            assertEquals(testUserDTO.getEmail(), response.getEmail());
            assertNotNull(response.getDireccion());
            assertEquals(testUserDTO.getDireccion().getLineaDireccion1(), response.getDireccion().getLineaDireccion1());
        });

        verify(userRepository).findById(anyInt());
    }

    @Test
    void C3_getUserProfile_should_throw_exception_when_user_not_found() {
        // Arrange
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> userService.getUserProfile(2));

        // Assert
        assertEquals("data_not_found.user", exception.getCode());
        verify(userRepository).findById(anyInt());
    }

    @Test
    void C4_updateProfile_should_return_updated_user() {
        // Arrange
        UserDTO updateRequest = UserDTO.builder()
                .id(1)
                .nombre("Mario")
                .apellidos("Hernández")
                .email("mario@salus.com")
                .nif("12345678A")
                .telefono("623711920")
                .fechaNacimiento(LocalDate.of(1991, 3, 23))
                .build();

        UserEntity updatedUser = UserEntity.builder()
                .id(1)
                .nombre("Mario")
                .apellidos("Hernández")
                .email("mario@salus.com")
                .nif("12345678A")
                .telefono("623711920")
                .fechaNacimiento(LocalDate.of(1991, 3, 23))
                .direccion(testDireccion)
                .rolesList(new ArrayList<>(List.of(RoleType.USER)))
                .accountStatusType(AccountStatusType.NOT_VERIFIED) // Al cambiar el email, se cambia el estado de la cuenta
                .restricted(false)
                .loginAttempts(0)
                .password("password")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByNif(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedUser);

        // Act
        UserDTO response = assertDoesNotThrow(() -> userService.updateProfile(1, updateRequest));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(updateRequest.getId(), response.getId());
            assertEquals(updateRequest.getNombre(), response.getNombre());
            assertEquals(updateRequest.getApellidos(), response.getApellidos());
            assertEquals(updateRequest.getEmail(), response.getEmail());
            assertEquals(updateRequest.getTelefono(), response.getTelefono());
        });

        verify(userRepository).findById(anyInt());
        verify(userRepository).findByEmail(anyString());
        verify(userRepository).findByNif(anyString());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void C5_updateProfile_should_throw_exception_when_email_already_exists() {
        // Arrange
        UserDTO updateRequest = UserDTO.builder()
                .id(1)
                .nombre("Mario")
                .apellidos("Hernández")
                .email("mario@salus.com")
                .nif("12345678A")
                .build();

        UserEntity existingUser = UserEntity.builder()
                .id(2)
                .email("mario@salus.com")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("mario@salus.com")).thenReturn(Optional.of(existingUser));

        // Act
        ConflictException exception = assertThrows(ConflictException.class, () -> userService.updateProfile(1, updateRequest));

        // Assert
        assertEquals("conflict.email_is_registered", exception.getCode());

        verify(userRepository).findById(anyInt());
        verify(userRepository).findByEmail(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void C6_updateAddress_should_return_user_with_updated_address() {
        // Arrange
        DireccionDTO updateAddressRequest = DireccionDTO.builder()
                .lineaDireccion1("Calle Test 33")
                .lineaDireccion2("")
                .codigoPostal("03181")
                .pais("España")
                .provincia("Alicante")
                .municipio("Torrevieja")
                .localidad("Torrevieja")
                .build();

        UserEntity updatedUser = UserEntity.builder()
                .id(1)
                .nombre("Esteban")
                .apellidos("Pérez")
                .email("steve@salus.com")
                .nif("12345678A")
                .telefono("637199234")
                .fechaNacimiento(LocalDate.of(1997, 6, 12))
                .direccion(modelMapper.map(updateAddressRequest, DireccionEmbeddable.class))
                .rolesList(new ArrayList<>(List.of(RoleType.USER)))
                .accountStatusType(AccountStatusType.VERIFIED)
                .restricted(false)
                .loginAttempts(0)
                .password("password")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedUser);

        // Act
        UserDTO response = assertDoesNotThrow(() -> userService.updateAddress(1, updateAddressRequest));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testUser.getId(), response.getId());
            assertNotNull(response.getDireccion());
            assertEquals(updateAddressRequest.getLineaDireccion1(), response.getDireccion().getLineaDireccion1());
            assertEquals(updateAddressRequest.getCodigoPostal(), response.getDireccion().getCodigoPostal());
        });

        verify(userRepository).findById(anyInt());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void C7_restrictUser_should_restrict_user_from_creating_new_appointments() {
        // Arrange
        RestrictUserRequest restrictRequest = RestrictUserRequest.builder()
                .restrict(true)
                .build();

        UserEntity restrictedUser = UserEntity.builder()
                .id(1)
                .nombre("Francisco")
                .apellidos("Gómez")
                .email("francisgo@salus.com")
                .nif("12345678A")
                .telefono("692882710")
                .restricted(true)
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(restrictedUser);

        // Act
        UserDTO response = userService.restrictUser(1, restrictRequest);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testUser.getId(), response.getId());
            assertTrue(response.getRestricted());
        });

        verify(userRepository).findById(anyInt());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void C8_getDoctorPatients_should_return_doctor_patients() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testMedicalProfile));
        when(userRepository.findDoctorPatients(any(MedicalProfileEntity.class), any(PageRequest.class))).thenReturn(testPagedUsers);
        when(userRepository.countDoctorPatients(any(MedicalProfileEntity.class))).thenReturn(1);

        // Act
        UsersListResponse response = userService.getDoctorPatients(1, 0, 10);

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(1, response.getCount());
            assertEquals(1, response.getUsers().size());
            assertEquals(testUserDTO.getId(), response.getUsers().get(0).getId());
        });

        verify(medicalProfileRepository).findById(anyInt());
        verify(userRepository).findDoctorPatients(any(MedicalProfileEntity.class), any(PageRequest.class));
        verify(userRepository).countDoctorPatients(any(MedicalProfileEntity.class));
    }

    @Test
    void C9_getDoctorPatients_throw_exception_when_medical_profile_not_found() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> userService.getDoctorPatients(1, 0, 10));

        // Assert
        assertEquals("data_not_found.doctor", exception.getCode());

        verify(medicalProfileRepository).findById(anyInt());
        verify(userRepository, never()).findDoctorPatients(any(MedicalProfileEntity.class), any(PageRequest.class));
    }

    @Test
    void C10_createUser_should_create_new_user() {
        // Arrange
        DireccionDTO direccionDTO = DireccionDTO.builder()
                .lineaDireccion1("Calle Nueva 2")
                .codigoPostal("03001")
                .pais("España")
                .provincia("Alicante")
                .municipio("Alicante")
                .localidad("Alicante")
                .build();

        CreateUserRequest createRequest = CreateUserRequest.builder()
                .nombre("Carlos")
                .apellidos("López")
                .email("carlosl@salus.com")
                .nif("12345678Z")
                .telefono("625188293")
                .fechaNacimiento(LocalDate.of(2000, 5, 20))
                .direccion(direccionDTO)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNif(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        PasswordResetEntity passwordReset = new PasswordResetEntity();
        passwordReset.setToken("test-token");
        when(authService.createPasswordResetToken(any(UserEntity.class))).thenReturn(passwordReset);
        doNothing().when(emailingService).sendTemplateEmail(anyString(), anyString(), anyString(), anyMap());

        // Act
        UserDTO response = assertDoesNotThrow(() -> userService.createUser(createRequest));

        // Assert
        assertAll(() -> {
            assertNotNull(response);
            assertEquals(testUser.getId(), response.getId());
            assertEquals(testUser.getNombre(), response.getNombre());
            assertEquals(testUser.getEmail(), response.getEmail());
        });

        verify(userRepository).existsByEmail(anyString());
        verify(userRepository).existsByNif(anyString());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(UserEntity.class));
        verify(authService).createPasswordResetToken(any(UserEntity.class));
        verify(emailingService).sendTemplateEmail(anyString(), anyString(), anyString(), anyMap());
    }
}