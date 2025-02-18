package com.kikisito.salus.api.controllers;

import com.kikisito.salus.api.dto.DireccionDTO;
import com.kikisito.salus.api.dto.UsuarioDTO;
import com.kikisito.salus.api.service.UserService;
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

    @GetMapping("/@me")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<UsuarioDTO> getCurrentProfile() {
        return ResponseEntity.ok(userService.getCurrentProfile());
    }

    @PostMapping("/@me/address")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<UsuarioDTO> updateAddress(@RequestBody DireccionDTO direccionDTO) {
        return ResponseEntity.ok(userService.updateAddress(direccionDTO));
    }
}
