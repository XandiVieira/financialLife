package com.relyon.financiallife.controller;

import com.relyon.financiallife.model.authentication.dto.AuthenticationRequest;
import com.relyon.financiallife.model.authentication.dto.SuccessfulAuthenticationResponse;
import com.relyon.financiallife.service.AuthenticationService;
import com.relyon.financiallife.service.BlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authService;
    @Mock
    private BlacklistService blacklistService;

    @InjectMocks
    private AuthenticationController controller;

    @Test
    void authenticate_ShouldReturnAccessToken() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        SuccessfulAuthenticationResponse response = SuccessfulAuthenticationResponse.builder()
                .token("token")
                .build();

        ResponseEntity<?> responseEntity = ResponseEntity.ok(response);
        doReturn(responseEntity).when(authService).authenticate(request);

        ResponseEntity<?> result = controller.authenticate(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(authService).authenticate(request);
    }


    @Test
    void logout_ShouldRevokeToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Object> result = controller.logout(request);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(blacklistService).revokeToken(request);
    }
}