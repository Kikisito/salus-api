package com.kikisito.salus.api.service;

import com.kikisito.salus.api.entity.SessionEntity;
import com.kikisito.salus.api.repository.SessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${application.security.jwt.password-reset-token.expiration}")
    private long passwordResetTokenExpiration;

    private final SessionRepository sessionRepository;

    public Optional<SessionEntity> findByAccessTokenAndRefreshTokenAndPublicId(String jwt) {
        return sessionRepository.findByAccessTokenAndRefreshTokenAndPublicId(extractAccessToken(jwt), extractRefreshToken(jwt), extractPublicId(jwt)
        );
    }

    @Transactional
    public void saveSession(SessionEntity session) {
        sessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(String jwt) {
        sessionRepository.deleteByPublicId(extractPublicId(jwt));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractAccessToken(String jwt) {
        final Claims claims = extractAllClaims(jwt);
        return claims.get("accessToken", String.class);
    }

    public String extractRefreshToken(String jwt) {
        final Claims claims = extractAllClaims(jwt);
        return claims.get("refreshToken", String.class);
    }

    public String extractPublicId(String jwt) {
        final Claims claims = extractAllClaims(jwt);
        return claims.get("publicId", String.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshTokenExpiration);
    }

    public String generatePasswordResetToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, passwordResetTokenExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiration == 0 ? null : new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateJWT(UserDetails userDetails, String accessToken, String refreshToken, String publicId) {
        var claims = new HashMap<String, Object>();
        claims.put("accessToken", accessToken);
        claims.put("refreshToken", refreshToken);
        claims.put("publicId", publicId);
        return buildToken(claims, userDetails, 0);
    }

    public String generateEmailVerificationToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .signWith(getSigningKey())
                .compact();
    }

    public String getNewJWT(UserDetails userDetails, SessionEntity session) {
        return generateJWT(userDetails, session.getAccessToken(), session.getRefreshToken(), session.getPublicId());
    }

    public boolean isTokenValid(String jwt) {
        return !extractUsername(jwt).isBlank()
                && !extractAccessToken(jwt).isBlank()
                && !extractRefreshToken(jwt).isBlank()
                && !extractPublicId(jwt).isBlank();
    }

    public boolean isTokenExpired(String jwt) {
        try {
            extractExpiration(jwt);
            return false;
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}