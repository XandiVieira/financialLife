package com.relyon.financiallife.exception;

import com.relyon.financiallife.exception.custom.ForbiddenException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.validation.UnexpectedTypeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.WebRequest;
import org.webjars.NotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleNotFoundException_ShouldReturnNotFoundResponse() {
        NotFoundException exception = new NotFoundException("Resource not found");
        ResponseEntity<Object> response = globalExceptionHandler.handleNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorsResponse responseBody = (ErrorsResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseBody.getStatus());
        assertEquals("Resource not found", responseBody.getMessage());
    }

    @Test
    void handleForbiddenException_ShouldReturnForbiddenResponse() {
        ForbiddenException exception = new ForbiddenException("Access denied");
        ResponseEntity<Object> response = globalExceptionHandler.handleAuthenticationFailedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorsResponse responseBody = (ErrorsResponse) response.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), responseBody.getStatus());
        assertEquals("Access denied", responseBody.getMessage());
    }

    @Test
    void handleAuthenticationAccessDeniedException_ShouldReturnAccessDeniedResponse() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        ResponseEntity<Object> response = globalExceptionHandler.handleAuthenticationAccessDeniedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorsResponse responseBody = (ErrorsResponse) response.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), responseBody.getStatus());
        assertEquals("Access denied", responseBody.getMessage());
    }

    @Test
    void handleAuthenticationMalformedJwtException_ShouldReturnMalformedJwtResponse() {
        MalformedJwtException exception = new MalformedJwtException("Access denied");
        ResponseEntity<Object> response = globalExceptionHandler.handleMalformedJwtException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorsResponse responseBody = (ErrorsResponse) response.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), responseBody.getStatus());
        assertEquals("Access denied", responseBody.getMessage());
    }

    @Test
    void handleAuthenticationUnexpectedTypeException_ShouldReturnUnexpectedTypeResponse() {
        UnexpectedTypeException exception = new UnexpectedTypeException();
        ResponseEntity<ErrorsResponse> response = globalExceptionHandler.handleUnexpectedTypeException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorsResponse responseBody = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseBody.getStatus());
    }

    @Test
    void handleInternalServerException_ShouldReturnInternalServerErrorResponse() {
        Exception exception = new Exception("Internal server error");
        ResponseEntity<Object> response = globalExceptionHandler.handleInternalServerException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorsResponse responseBody = (ErrorsResponse) response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseBody.getStatus());
        assertEquals("Internal server error", responseBody.getMessage());
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnIllegalArgumentResponse() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid or expired token");
        ResponseEntity<Object> response = globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleIllegalArgumentException_WithUnexpectedCase_ShouldReturnIllegalArgumentResponse() {
        IllegalArgumentException exception = new IllegalArgumentException("unexpected flow");
        assertThrows(IllegalArgumentException.class, () -> globalExceptionHandler.handleIllegalArgumentException(exception, webRequest));
    }

    @Test
    void handleDataIntegrityViolationException_ShouldReturnConflictResponse() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data integrity violation exception");
        ResponseEntity<Object> response = globalExceptionHandler.handleDataIntegrityViolationException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorsResponse responseBody = (ErrorsResponse) response.getBody();
        assertEquals(HttpStatus.CONFLICT.value(), responseBody.getStatus());
        assertEquals("Duplicated key", responseBody.getMessage());
    }
}