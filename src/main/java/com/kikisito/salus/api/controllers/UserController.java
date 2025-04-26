package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.DireccionDTO;
import com.kikisito.salus.api.dto.UserDTO;
import com.kikisito.salus.api.dto.request.CreateUserRequest;
import com.kikisito.salus.api.dto.request.PasswordChangeRequest;
import com.kikisito.salus.api.dto.request.RestrictUserRequest;
import com.kikisito.salus.api.dto.response.AuthenticationResponse;
import com.kikisito.salus.api.dto.response.UsersListResponse;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.service.AuthService;
import com.kikisito.salus.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
    @Autowired
    private final UserService userService;

    @Autowired
    private final AuthService authService;

    private static final int MAX_ROWS_PER_PAGE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @GetMapping(value = { "/all", "/all/{page}", "/all/{page}/{limit}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UsersListResponse> getAllUsers(@PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        // Validación antes de realizar la consulta
        if(page.isPresent() && page.get() < 0) {
            return ResponseEntity.badRequest().build();
        }

        // Limitamos el número de usuarios a mostrar en [1,MAX_ROWS_PER_PAGE] para evitar problemas de rendimiento
        if(limit.isPresent() && (limit.get() < 1 || limit.get() > MAX_ROWS_PER_PAGE)) {
            return ResponseEntity.badRequest().build();
        }

        // Obtenemos la lista de usuarios
        UsersListResponse users;
        if(page.isPresent() && limit.isPresent()) {
            users = userService.getAllUsers(page.get(), limit.get());
        } else if(page.isPresent()) {
            users = userService.getAllUsers(page.get(), DEFAULT_PAGE_SIZE);
        } else {
            users = userService.getAllUsers(0, DEFAULT_PAGE_SIZE);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search/{search}/{page}/{limit}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UsersListResponse> searchUsers(@PathVariable String search, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        // Validación antes de realizar la consulta
        if(page.isPresent() && page.get() < 0) {
            return ResponseEntity.badRequest().build();
        }

        // Limitamos el número de usuarios a mostrar en [1,MAX_ROWS_PER_PAGE] para evitar problemas de rendimiento
        if(limit.isPresent() && (limit.get() < 1 || limit.get() > MAX_ROWS_PER_PAGE)) {
            return ResponseEntity.badRequest().build();
        }

        // Obtenemos la lista de usuarios
        UsersListResponse users;
        if(page.isPresent() && limit.isPresent()) {
            users = userService.searchUsers(search, page.get(), limit.get());
        } else if(page.isPresent()) {
            users = userService.searchUsers(search, page.get(), DEFAULT_PAGE_SIZE);
        } else {
            users = userService.searchUsers(search, 0, DEFAULT_PAGE_SIZE);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping(value = { "/patients/{doctorId}", "/patients/{doctorId}/{page}", "/patients/{doctorId}/{page}/{limit}"})
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and authentication.principal.medicalProfile.id == #doctorId)")
    public ResponseEntity<UsersListResponse> getDoctorPatients(@PathVariable Integer doctorId, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        // Validación antes de realizar la consulta
        if(page.isPresent() && page.get() < 0) {
            return ResponseEntity.badRequest().build();
        }

        // Limitamos el número de usuarios a mostrar en [1,MAX_ROWS_PER_PAGE] para evitar problemas de rendimiento
        if(limit.isPresent() && (limit.get() < 1 || limit.get() > MAX_ROWS_PER_PAGE)) {
            return ResponseEntity.badRequest().build();
        }

        // Obtenemos la lista de usuarios
        UsersListResponse users;
        if(page.isPresent() && limit.isPresent()) {
            users = userService.getDoctorPatients(doctorId, page.get(), limit.get());
        } else if(page.isPresent()) {
            users = userService.getDoctorPatients(doctorId, page.get(), DEFAULT_PAGE_SIZE);
        } else {
            users = userService.getDoctorPatients(doctorId, 0, DEFAULT_PAGE_SIZE);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/patients/search/{search}/{doctorId}/{page}/{limit}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and authentication.principal.medicalProfile.id == #doctorId)")
    public ResponseEntity<UsersListResponse> searchDoctorPatients(@PathVariable Integer doctorId, @PathVariable String search, @PathVariable Optional<Integer> page, @PathVariable Optional<Integer> limit) {
        // Validación antes de realizar la consulta
        if(page.isPresent() && page.get() < 0) {
            return ResponseEntity.badRequest().build();
        }

        // Limitamos el número de usuarios a mostrar en [1,MAX_ROWS_PER_PAGE] para evitar problemas de rendimiento
        if(limit.isPresent() && (limit.get() < 1 || limit.get() > MAX_ROWS_PER_PAGE)) {
            return ResponseEntity.badRequest().build();
        }

        // Obtenemos la lista de usuarios
        UsersListResponse users;
        if(page.isPresent() && limit.isPresent()) {
            users = userService.getDoctorPatientsBySearch(doctorId, search, page.get(), limit.get());
        } else if(page.isPresent()) {
            users = userService.getDoctorPatientsBySearch(doctorId, search, page.get(), DEFAULT_PAGE_SIZE);
        } else {
            users = userService.getDoctorPatientsBySearch(doctorId, search, 0, DEFAULT_PAGE_SIZE);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROFESSIONAL') and @appointmentService.patientHasAtLeastOneAppointmentWithDoctor(#userId, authentication.principal.medicalProfile.id))")
    public ResponseEntity<UserDTO> getUserById(@PathVariable int userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable("id") Integer userId, @RequestBody @Valid UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateProfile(userId, userDTO));
    }

    @PutMapping("/{id}/address")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> updateAddress(@PathVariable("id") Integer userId, @RequestBody @Valid DireccionDTO direccionDTO) {
        return ResponseEntity.ok(userService.updateAddress(userId, direccionDTO));
    }

    @PutMapping("/{id}/restrict")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> restrictUser(@PathVariable("id") Integer userId, @RequestBody @Valid RestrictUserRequest request) {
        return ResponseEntity.ok(userService.restrictUser(userId, request));
    }

    // Sección para el usuario actual

    @GetMapping("/@me")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<UserDTO> getCurrentProfile(@AuthenticationPrincipal UserEntity userEntity) {
        return ResponseEntity.ok(userService.getUserProfile(userEntity));
    }

    @PatchMapping("/@me")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<UserDTO> updateCurrentProfile(@AuthenticationPrincipal UserEntity userEntity, @RequestBody @Valid UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateProfile(userEntity , userDTO));
    }

    @PutMapping("/@me/address")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<UserDTO> updateAddress(@AuthenticationPrincipal UserEntity userEntity, @RequestBody @Valid DireccionDTO direccionDTO) {
        return ResponseEntity.ok(userService.updateAddress(userEntity, direccionDTO));
    }

    @PutMapping("/@me/password")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<AuthenticationResponse> updatePassword(@RequestBody @Valid PasswordChangeRequest passwordChangeRequest) {
        return ResponseEntity.ok(
                authService.changePassword(passwordChangeRequest.getCurrentPassword(), passwordChangeRequest.getPassword())
        );
    }

    @DeleteMapping("/@me/sessions")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Void> logout() {
        authService.closeAllSessions();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> addUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        return ResponseEntity.ok(userService.createUser(createUserRequest));
    }
}
