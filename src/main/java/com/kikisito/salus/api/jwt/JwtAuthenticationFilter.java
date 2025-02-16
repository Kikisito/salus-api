package com.kikisito.salus.api.jwt;

import com.kikisito.salus.api.entity.SessionEntity;
import com.kikisito.salus.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        // Permitir que las solicitudes a los endpoints de recuperación de contraseña pasen sin necesidad de un token
        // Permitir que las solicitudes al endpoint /verify pasen sin necesidad de un token JWT
        if (request.getRequestURI().startsWith("/api/v1/auth/password-reset/") ||
            request.getRequestURI().startsWith("/api/v1/auth/verify")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // Si no hay un token JWT en el encabezado de autorización, continuar con la cadena de filtros
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el token JWT del encabezado de autorización
        final String jwt = authHeader.substring(7);

        // Si el token JWT no es válido, cerramos la sesión y continuamos con la cadena de filtros
        if(!jwtService.isTokenValid(jwt)) {
            closeSession(jwt);
            filterChain.doFilter(request, response);
            return;
        }

        // Si el token JWT es válido y comprueba que el usuario no está autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Buscamos la sesión en la base de datos. Si no existe, cerramos la sesión y continuamos con la cadena de filtros
        Optional<SessionEntity> sessionEntityOptional = jwtService.findByAccessTokenAndRefreshTokenAndPublicId(jwt);
        if(sessionEntityOptional.isEmpty()) {
            closeSession(jwt);
            filterChain.doFilter(request, response);
            return;
        }

        SessionEntity sessionEntity = sessionEntityOptional.get();
        // Extraemos el email del usuario del token JWT y cargamos los detalles del usuario
        final String userEmail = jwtService.extractUsername(jwt);
        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

        // Si el token de acceso ha expirado...
        String newToken = jwt;
        if (jwtService.isTokenExpired(sessionEntity.getAccessToken())) {
            // ...y si el token de refresh también ha expirado, se cierra la sesión
            if (jwtService.isTokenExpired(sessionEntity.getRefreshToken())) {
                closeSession(jwt);
                return;
            }
            // ...y si el token de refresh no ha expirado, se genera un nuevo token de acceso
            sessionEntity.setRefreshToken(jwtService.generateRefreshToken(userDetails));
            sessionEntity.setAccessToken(jwtService.generateAccessToken(userDetails));
            jwtService.saveSession(sessionEntity);
            newToken = jwtService.getNewJWT(userDetails, sessionEntity);
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        response.setHeader("Authorization", "Bearer " + newToken);

        // Continuamos la cadena de filtros
        filterChain.doFilter(request, response);
    }

    private void closeSession(String jwt) {
        // Eliminamos la sesión de la base de datos
        jwtService.deleteSession(jwt);
    }
}