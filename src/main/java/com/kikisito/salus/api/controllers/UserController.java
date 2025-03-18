package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.DireccionDTO;
import com.kikisito.salus.api.dto.UsuarioDTO;
import com.kikisito.salus.api.dto.request.PasswordChangeRequest;
import com.kikisito.salus.api.dto.response.AuthenticationResponse;
import com.kikisito.salus.api.dto.response.UsersListResponse;
import com.kikisito.salus.api.service.AuthService;
import com.kikisito.salus.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping(value = { "/all", "/all/{page}", "/all/{page}/{count}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UsersListResponse> getAllUsers(@PathVariable Optional<Integer> page, @PathVariable Optional<Integer> count) {
        // Validación antes de realizar la consulta
        if(page.isPresent() && page.get() < 0) {
            return ResponseEntity.badRequest().build();
        }

        // Limitamos el número de usuarios a mostrar en [1,100] para evitar problemas de rendimiento
        if(count.isPresent() && (count.get() < 1 || count.get() > 100)) {
            return ResponseEntity.badRequest().build();
        }

        // Obtenemos la lista de usuarios
        UsersListResponse users;
        if(page.isPresent() && count.isPresent()) {
            users = userService.getAllUsers(page.get(), count.get());
        } else if(page.isPresent()) {
            users = userService.getAllUsers(page.get(), 10);
        } else {
            users = userService.getAllUsers(0, 10);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/@me")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<UsuarioDTO> getCurrentProfile() {
        return ResponseEntity.ok(userService.getCurrentProfile());
    }

    @PatchMapping("/@me")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<UsuarioDTO> updateProfile(@RequestBody @Valid UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(userService.updateProfile(usuarioDTO));
    }

    @PutMapping("/@me/address")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<UsuarioDTO> updateAddress(@RequestBody @Valid DireccionDTO direccionDTO) {
        return ResponseEntity.ok(userService.updateAddress(direccionDTO));
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
}
