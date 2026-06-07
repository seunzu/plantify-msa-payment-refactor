package com.plantify.payment.config;

import com.plantify.payment.client.AuthServiceClient;
import com.plantify.payment.global.AuthUserResponse;
import com.plantify.payment.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        if (token != null) {
            try {
                ApiResponse<AuthUserResponse> authResponse = authServiceClient.getUserInfo("Bearer " + token);

                if (authResponse.success() && authResponse.data() != null) {
                    AuthUserResponse userResponse = authResponse.data();
                    Authentication authentication = getAuthentication(userResponse, token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {}
        }
        filterChain.doFilter(request, response);
    }

    private Authentication getAuthentication(AuthUserResponse userResponse, String token) {
        return new UsernamePasswordAuthenticationToken(
                userResponse.userId(),
                token,
                List.of(new SimpleGrantedAuthority("ROLE_" + userResponse.role()))
        );
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
