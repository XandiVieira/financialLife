package com.relyon.financiallife.configuration.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationAuditorAwareTest {

    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ApplicationAuditorAware auditorAware;

    @Test
    void getCurrentAuditor_WithoutAuthentication_ShouldReturnEmptyOptional() {
        SecurityContextHolder.clearContext();
        assertEquals(Optional.empty(), auditorAware.getCurrentAuditor());
    }

    @Test
    void getCurrentAuditor_WithAuthentication_ShouldReturnUsername() {
        String username = "testuser";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn(username);
        when(authentication.isAuthenticated()).thenReturn(true);

        assertEquals(Optional.of(username), auditorAware.getCurrentAuditor());
    }
}