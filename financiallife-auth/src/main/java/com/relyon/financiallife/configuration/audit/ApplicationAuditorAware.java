package com.relyon.financiallife.configuration.audit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
@Slf4j
public class ApplicationAuditorAware implements AuditorAware<String> {

    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        log.info("Getting current auditor....");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.info("No authenticated user found for auditing.");
            return Optional.empty();
        }
        String username = authentication.getName();
        log.info("Auditing operation performed by user: {}", username);
        return Optional.of(username);
    }
}