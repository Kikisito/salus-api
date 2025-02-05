package com.kikisito.salus.api.jwt;

import com.kikisito.salus.api.entity.SessionEntity;
import com.kikisito.salus.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${application.security.jwt.cookieMaxAge}")
    private int cookieMaxAge;

    private static final String COOKIE_NAME = "AUTH-TOKEN";
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

        // Buscamos la cookie de token en la solicitud
        Optional<Cookie> tokenCookie = Optional.ofNullable(request.getCookies()).stream().flatMap(Arrays::stream)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst();

        // Si no hay una cookie de token, continuar con la cadena de filtros
        if(tokenCookie.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el token JWT de la cookie
        final String jwt = tokenCookie.get().getValue();

        // Si el token JWT no es válido, cerramos la sesión y continuamos con la cadena de filtros
        if(!jwtService.isTokenValid(jwt)) {
            closeSession(jwt, response);
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
            closeSession(jwt, response);
            filterChain.doFilter(request, response);
            return;
        }

        SessionEntity sessionEntity = sessionEntityOptional.get();
        // Extraemos el email del usuario del token JWT y cargamos los detalles del usuario
        final String userEmail = jwtService.extractUsername(jwt);
        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

        // Si el token de acceso ha expirado...
        if (jwtService.isTokenExpired(sessionEntity.getAccessToken())) {
            // ...y si el token de refresh también ha expirado, se cierra la sesión
            if (jwtService.isTokenExpired(sessionEntity.getRefreshToken())) {
                closeSession(jwt, response);
                return;
            }
            // ...y si el token de refresh no ha expirado, se genera un nuevo token de acceso
            sessionEntity.setRefreshToken(jwtService.generateRefreshToken(userDetails));
            sessionEntity.setAccessToken(jwtService.generateAccessToken(userDetails));
            jwtService.saveSession(sessionEntity);
            String newToken = jwtService.getNewJWT(userDetails, sessionEntity);

            // Actualizamos la cookie con el nuevo token JWT
            Cookie newCookie = createJwtCookie(newToken);
            response.addCookie(newCookie);
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Continuamos la cadena de filtros
        filterChain.doFilter(request, response);
    }

    private Cookie createJwtCookie(String jwt) {
        Cookie cookie = new Cookie(COOKIE_NAME, jwt);
        cookie.setMaxAge(this.cookieMaxAge); // 7 días
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(true);
        return cookie;
    }

    private void closeSession(String jwt, HttpServletResponse response) {
        // Eliminamos la sesión de la base de datos
        jwtService.deleteSession(jwt);

        // Eliminamos la cookie de token
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}