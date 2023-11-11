package com.relyon.financiallife.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigurationTest {

    @Mock
    private AuthenticationConfiguration authConfig;

    @InjectMocks
    private ApplicationConfiguration appConfig;

    @Test
    void authenticationManager_ShouldReturnAuthenticationManagerFromAuthenticationConfiguration() throws Exception {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(authManager);
        assertEquals(authManager, appConfig.authenticationManager(authConfig));
    }

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        assertTrue(appConfig.passwordEncoder() instanceof BCryptPasswordEncoder);
    }

    @Test
    void logger_ShouldReturnLoggerWithNameLogger() {
        Logger logger = appConfig.logger();
        assertEquals("logger", logger.getName());
    }

    @Test
    void methodValidationPostProcessor_ShouldReturnNewInstance() {
        MethodValidationPostProcessor processor = appConfig.methodValidationPostProcessor();
        assertNotNull(processor);
    }

}
