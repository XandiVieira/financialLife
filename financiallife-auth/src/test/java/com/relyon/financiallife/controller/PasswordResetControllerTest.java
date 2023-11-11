package com.relyon.financiallife.controller;

import com.relyon.financiallife.model.password.dto.PasswordResetEmailResponse;
import com.relyon.financiallife.model.password.dto.PasswordResetResponse;
import com.relyon.financiallife.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    @Test
    void processForgotPasswordForm_ShouldReturn200() {
        String email = "user@example.com";
        PasswordResetEmailResponse passwordResetResponse = new PasswordResetEmailResponse();
        passwordResetResponse.setMessage("Password reset email sent successfully");

        when(passwordResetService.sendPasswordResetEmail(email)).thenReturn(ResponseEntity.ok(passwordResetResponse));

        ResponseEntity<PasswordResetEmailResponse> responseEntity = passwordResetController.processForgotPasswordForm(email);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(passwordResetResponse, responseEntity.getBody());
    }

    @Test
    void processResetPasswordForm_ShouldReturn200() {
        String tokenValue = "some-token";
        String password = "new-password";
        PasswordResetResponse passwordResetResponse = new PasswordResetResponse();
        passwordResetResponse.setMessage("Password reset successfully");

        when(passwordResetService.resetPassword(tokenValue, password, password)).thenReturn(ResponseEntity.ok(passwordResetResponse));

        ResponseEntity<PasswordResetResponse> responseEntity = passwordResetController.processResetPasswordForm(tokenValue, password, password);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(passwordResetResponse, responseEntity.getBody());
    }
}
