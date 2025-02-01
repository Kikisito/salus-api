package com.kikisito.salus.api.jwt;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${application.security.jwt.cookieMaxAge}")
    private int cookieMaxAge;

    private static final String COOKIE_NAME = "auth-token";
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        // Permitir que las solicitudes a los endpoints de recuperación de contraseña pasen sin necesidad de un token
        if (request.getRequestURI().startsWith("/api/v1/auth/password-reset/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Permitir que las solicitudes al endpoint /verify pasen sin necesidad de un token JWT
        if ("/api/v1/auth/verify".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Cookie> tokenCookie = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                        .findFirst()
                );

        // Si no hay una cookie de token, continuar con la cadena de filtros
        if(tokenCookie.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el token JWT de la cookie
        final String jwt = tokenCookie.get().getValue();

        // Si el token JWT es válido y comprueba que el usuario no está autenticado
        if (jwtService.isTokenValid(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.findByAccessTokenAndRefreshTokenAndPublicId(jwt).ifPresentOrElse(
                sessionEntity -> {
                    // Extraemos el email del usuario del token JWT y cargamos los detalles del usuario
                    final var userEmail = jwtService.extractUsername(jwt);
                    final UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                    // Si el token JWT ha expirado, generamos un nuevo token JWT y lo devolvemos en el encabezado de respuesta
                    // Si el token de refresh también ha expirado, cerramos directamente la sesión
                    var newToken = jwt;
                    if (jwtService.isTokenExpired(sessionEntity.getAccessToken())) {
                        if (jwtService.isTokenExpired(sessionEntity.getRefreshToken())) {
                            closeSession(jwt, response);
                            return;
                        }
                        sessionEntity.setRefreshToken(jwtService.generateRefreshToken(userDetails));
                        sessionEntity.setAccessToken(jwtService.generateAccessToken(userDetails));
                        jwtService.saveSession(sessionEntity);
                        newToken = jwtService.getNewJWT(userDetails, sessionEntity);

                        // Actualizamos la cookie con el nuevo token JWT
                        Cookie newCookie = createJwtCookie(newToken);
                        response.addCookie(newCookie);
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                },
                () -> closeSession(jwt, response)
            );
        }

        // Continuamos la cadena de filtros
        filterChain.doFilter(request, response);
    }

    private Cookie createJwtCookie(String jwt) {
        Cookie cookie = new Cookie(COOKIE_NAME, jwt);
        cookie.setMaxAge(this.cookieMaxAge); // 7 días
        cookie.setPath("/");
        cookie.setHttpOnly(true);
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