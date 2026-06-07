package com.plantify.pay.config;

import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.AuthErrorCode;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @PostConstruct
    protected void init() {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createAccessToken(long userId) {
        return generateToken(userId, "Access", accessTokenExpiration);
    }

    private String generateToken(Long id, String type, long tokenValidTime) {
        Claims claims = Jwts.claims().setSubject(type);
        claims.put("id", id);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ApplicationException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new ApplicationException(AuthErrorCode.UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException | IllegalArgumentException e) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        } catch (JwtException e) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new ApplicationException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException e) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    public String extractTokenFromHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
    }
}
