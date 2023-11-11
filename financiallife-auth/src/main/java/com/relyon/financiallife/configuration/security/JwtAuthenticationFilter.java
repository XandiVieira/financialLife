package com.relyon.financiallife.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relyon.financiallife.exception.ErrorsResponse;
import com.relyon.financiallife.service.BlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final BlacklistService blacklistService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String userEmail;
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Authorization token was not sent properly.");
            buildResponseError(response, "Authorization token was not sent properly.");
            return;
        }
        jwt = authHeader.substring(7);

        if (request.getRequestURI().equals("/api/v1/authentication/logout")) {
            blacklistService.revokeToken(request);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            log.info("User logged out successfully");
            return;
        }

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token.");
            buildResponseError(response, "Expired token");
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (blacklistService.isTokenRevoked(jwt)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or revoked token");
                log.warn("Invalid or revoked token. User email: {}", userEmail);
                return;
            }
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("User authenticated successfully. User email: {}", userEmail);
            } else {
                log.warn("Invalid JWT token. User email: {}", userEmail);
            }
        }
        filterChain.doFilter(request, response);
    }

    private static void buildResponseError(HttpServletResponse response, String message) throws IOException {
        log.warn("Building response error...");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        ObjectMapper mapper = new ObjectMapper();
        ErrorsResponse responseObject = new ErrorsResponse(HttpStatus.UNAUTHORIZED.value(), message);
        String json = mapper.writeValueAsString(responseObject);
        PrintWriter writer = response.getWriter();
        writer.write(json);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        List<String> excludedPaths = List.of("api-docs", "swagger", "authenticate", "password-reset");
        boolean isNotFiltered = excludedPaths.stream().anyMatch(path::contains);
        log.info("Path requested: {}, is filtered: {}", path, !isNotFiltered);
        return isNotFiltered;
    }
}