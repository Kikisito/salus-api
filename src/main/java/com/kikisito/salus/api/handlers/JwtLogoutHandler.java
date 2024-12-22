package com.kikisito.salus.api.handlers;

import com.kikisito.salus.api.repository.SessionRepository;
import com.kikisito.salus.api.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final SessionRepository sessionRepository;
    private final JwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        jwt = authHeader.substring(7);
        sessionRepository.findByAccessToken(jwtService.extractAccessToken(jwt))
                .ifPresent(sessionRepository::delete);
    }
}