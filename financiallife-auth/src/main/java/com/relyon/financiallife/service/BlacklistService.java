package com.relyon.financiallife.service;

import com.relyon.financiallife.model.authentication.revocation.Blacklist;
import com.relyon.financiallife.repository.BlacklistRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistService {

    private final BlacklistRepository repository;

    public void revokeToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token)) {
            repository.save(new Blacklist(token));
            log.info("Token revoked successfully.");
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.info("Token extracted from request.");
            return token;
        }
        throw new IllegalArgumentException("Invalid or expired token");
    }

    public boolean isTokenRevoked(String token) {
        boolean result = repository.existsByToken(token);
        log.info("Token is already revoked: {}", result);
        return result;
    }
}