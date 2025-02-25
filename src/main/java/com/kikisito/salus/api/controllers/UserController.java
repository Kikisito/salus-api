package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.DireccionDTO;
import com.kikisito.salus.api.dto.UsuarioDTO;
import com.kikisito.salus.api.dto.request.PasswordChangeRequest;
import com.kikisito.salus.api.dto.response.AuthenticationResponse;
import com.kikisito.salus.api.service.AuthService;
import com.kikisito.salus.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
    @Autowired
    private final UserService userService;

    @Autowired
    private final AuthService authService;

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
