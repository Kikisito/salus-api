package com.kikisito.salus.api.jwt;

import com.kikisito.salus.api.entity.SessionEntity;
import com.kikisito.salus.api.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtWebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token"); // el token se pasará por (URL)?token=...
            if (token == null || !jwtService.isTokenValid(token)) {
                return false;
            }

            // Buscamos la sesión en la base de datos. Si no existe, cerramos la sesión
            Optional<SessionEntity> sessionEntityOptional = jwtService.findByAccessTokenAndRefreshTokenAndPublicId(token);
            if(sessionEntityOptional.isEmpty()) {
                jwtService.deleteSession(token);
                return false;
            }
            SessionEntity sessionEntity = sessionEntityOptional.get();

            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Si el token de acceso ha expirado, no permitimos el acceso al websocket
            if(jwtService.isTokenExpired(sessionEntity.getAccessToken())){
                return false;
            }

            // Llegados a este punto, el usuario ya está correctamente autenticado
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // ignorado, solo está para cumplir con la interfaz
    }
}
