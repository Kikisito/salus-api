package com.kikisito.salus.api.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kikisito.salus.api.constants.ErrorMessages;
import com.kikisito.salus.api.dto.ErrorDetails;
import com.kikisito.salus.api.dto.response.ExceptionResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class AuthenticationExceptionHandler implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public AuthenticationExceptionHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        if(authException instanceof BadCredentialsException) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            ExceptionResponse exceptionResponse = new ExceptionResponse(
                    "User not found",
                    List.of(new ErrorDetails("generic.bad_credentials", ErrorMessages.USER_NOT_FOUND)),
                    LocalDateTime.now()
            );

            response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            ExceptionResponse exceptionResponse = new ExceptionResponse(
                    "Unauthorized",
                    List.of(new ErrorDetails("generic.unauthorized", ErrorMessages.NOT_AUTHORIZED)),
                    LocalDateTime.now()
            );

            response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
        }
    }
}
