package com.plantify.payment.global.jwt;

import com.plantify.payment.global.exception.errorcode.AuthErrorCode;
import com.plantify.payment.global.exception.ApplicationException;
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

    public String createPaymentToken(Long transactionId) {
        return generateToken(transactionId, accessTokenExpiration);
    }

    private String generateToken(Long transactionId, long tokenValidTime) {
        Claims claims = Jwts.claims().setSubject("Payment");
        claims.put("transactionId", transactionId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Long getTransactionId(String token) {
        return getClaims(token).get("transactionId", Long.class);
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
}
