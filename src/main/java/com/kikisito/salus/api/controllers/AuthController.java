package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.request.*;
import com.kikisito.salus.api.dto.response.AuthenticationResponse;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {
    @Autowired
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest registerRequest) {;
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/verification/verify")
    public ResponseEntity<HttpStatus> verifyEmailByToken(@RequestBody @Valid VerifyEmailRequest request) {
        authService.verifyEmailByToken(request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verification/resend")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<HttpStatus> resendVerification(@AuthenticationPrincipal UserEntity user) {
        authService.resendVerificationEmail(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<HttpStatus> recoverPassword(@RequestBody @Valid PasswordResetRecoverRequest request) {
        authService.recoverPassword(request.getToken(), request.getPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password/request")
    public ResponseEntity<HttpStatus> sendPasswordRecoveryMail(@RequestBody @Valid PasswordResetRequest request) {
        authService.startPasswordRecoveryProcess(request.getEmail(), request.getNif());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<HttpStatus> logout(@RequestHeader("Authorization") String rawToken) {
        String token = rawToken.substring(7);
        authService.logout(token);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/available/email")
    public ResponseEntity<HttpStatus> existsEmail(@RequestBody @Valid CheckEmailExistsRequest request) {
        authService.isEmailAvailable(request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/available/nif")
    public ResponseEntity<HttpStatus> existsNif(@RequestBody @Valid CheckNifExistsRequest request) {
        authService.isNifAvailable(request.getNif());
        return ResponseEntity.noContent().build();
    }
}
