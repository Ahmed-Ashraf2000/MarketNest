package com.marketnest.ecommerce.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.error.SimpleErrorResponse;
import com.marketnest.ecommerce.service.auth.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtTokenValidatorFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String jwt = request.getHeader("Authorization");

        if (jwt != null && jwt.startsWith("Bearer ")) {
            try {
                jwt = jwt.substring(7);

                Authentication auth = jwtService.validateToken(jwt);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (ExpiredJwtException e) {
                handleAuthError(response, "JWT token expired");
                return;
            } catch (UnsupportedJwtException e) {
                handleAuthError(response, "Unsupported JWT token");
                return;
            } catch (MalformedJwtException e) {
                handleAuthError(response, "Malformed JWT token");
                return;
            } catch (SignatureException e) {
                handleAuthError(response, "Invalid JWT signature");
                return;
            } catch (IllegalArgumentException e) {
                handleAuthError(response, "JWT claims string is empty");
                return;
            } catch (Exception e) {
                handleAuthError(response, "Invalid JWT token: " + e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleAuthError(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(),
                new SimpleErrorResponse(message));
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return request.getServletPath().equals("/api/auth/login");
    }
}