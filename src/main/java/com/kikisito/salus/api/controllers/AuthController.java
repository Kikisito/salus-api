package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.AuthenticationResponse;
import com.kikisito.salus.api.dto.LoginRequest;
import com.kikisito.salus.api.dto.RegisterRequest;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/verification/verify")
    public ResponseEntity<HttpStatus> verifyEmailByToken(@RequestParam("token") String token) {
        authService.verifyEmailByToken(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verification/resend")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<HttpStatus> resendVerification() {
        UserEntity userEntity = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authService.resendVerificationEmail(userEntity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recover")
    public ResponseEntity<HttpStatus> recoverPassword(@RequestParam("token") String token, @RequestBody @Valid String password) {
        authService.recoverPassword(token, password);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recover/send")
    public ResponseEntity<HttpStatus> sendPasswordRecoveryMail(@RequestBody @Valid String email) {
        authService.sendPasswordRecoveryMail(email);
        return ResponseEntity.ok().build();
    }

    // todo: implement logout
    @PostMapping("/logout")
    public void fakeLogout() {
        throw new IllegalStateException("This method shouldn't be called. It's implemented by Spring Security filters.");
    }
}
