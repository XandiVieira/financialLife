package com.relyon.financiallife.service;

import com.relyon.financiallife.model.authentication.revocation.Blacklist;
import com.relyon.financiallife.repository.BlacklistRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistServiceTest {

    @Mock
    private BlacklistRepository repository;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private BlacklistService service;

    @Test
    void revokeToken_ShouldRevokeTokenSuccessfully() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");

        service.revokeToken(request);

        verify(repository, times(1)).save(any(Blacklist.class));
    }

    @Test
    void revokeToken_WithInvalidToken_ShouldThrowIllegalArgumentException() {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.revokeToken(request));

        assertEquals("Invalid or expired token", exception.getMessage());
    }

    @Test
    void isTokenRevoked_WithExistingToken_ShouldReturnTrue() {
        String token = "token123";

        when(repository.existsByToken(token)).thenReturn(true);

        boolean isRevoked = service.isTokenRevoked(token);

        assertTrue(isRevoked);
        verify(repository, times(1)).existsByToken(token);
    }

    @Test
    void isTokenRevoked_WithNonExistingToken_ShouldReturnFalse() {
        String token = "token123";

        when(repository.existsByToken(token)).thenReturn(false);

        boolean isRevoked = service.isTokenRevoked(token);

        assertFalse(isRevoked);
        verify(repository, times(1)).existsByToken(token);
    }
}