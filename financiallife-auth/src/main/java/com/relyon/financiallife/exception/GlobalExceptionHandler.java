package com.relyon.financiallife.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.relyon.financiallife.exception.custom.*;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.UnexpectedTypeException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.webjars.NotFoundException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {NotFoundException.class})
    protected ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        log.error(ex.getMessage(), ex);
        ErrorsResponse exception = new ErrorsResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    protected ResponseEntity<Object> handleAuthenticationFailedException(ForbiddenException ex) {
        log.error(ex.getMessage(), ex);
        ErrorsResponse exception = new ErrorsResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    protected ResponseEntity<Object> handleAuthenticationAccessDeniedException(AccessDeniedException ex) {
        log.error(ex.getMessage(), ex);
        ErrorsResponse exception = new ErrorsResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {AccountLockedException.class})
    protected ResponseEntity<Object> handleAccountLockedException(AccountLockedException ex) {
        log.error(ex.getMessage(), ex);
        ErrorsResponse exception = new ErrorsResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {AccountDisabledException.class})
    protected ResponseEntity<Object> handleAccountDisabledException(AccountDisabledException ex) {
        log.error(ex.getMessage(), ex);
        ErrorsResponse exception = new ErrorsResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {MalformedJwtException.class})
    protected ResponseEntity<Object> handleMalformedJwtException(MalformedJwtException ex) {
        log.error(ex.getMessage(), ex);
        ErrorsResponse exception = new ErrorsResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleInternalServerException(Exception ex) {
        log.error(ex.getMessage(), ex);
        ErrorsResponse exception = new ErrorsResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return new ResponseEntity<>(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {DataAccessException.class})
    protected ResponseEntity<Object> handleDataAccessException(DataAccessException ex) {
        log.error(ex.getMessage(), ex);
        ErrorsResponse exception = new ErrorsResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error(ex.getMessage(), ex);
        FieldErrorResponse fieldErrorResponse = getFieldErrors(ex);
        ErrorsResponse errorResponse = new ErrorsResponse(HttpStatus.CONFLICT.value(), "Duplicated key", Collections.singletonList(fieldErrorResponse));
        if (isForeignKeyConstraintViolation(ex)) {
            errorResponse = new ErrorsResponse(HttpStatus.CONFLICT.value(), "Foreign key constraint violation", Collections.singletonList(fieldErrorResponse));
        }
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    private boolean isForeignKeyConstraintViolation(DataIntegrityViolationException ex) {
        return ex.getCause() instanceof SQLIntegrityConstraintViolationException sqlIntegrityException
                && sqlIntegrityException.getErrorCode() == 1452;
    }

    public FieldErrorResponse getFieldErrors(DataIntegrityViolationException ex) {
        log.error(ex.getMessage(), ex);
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof PSQLException) {
            String message = rootCause.toString();
            String[] errorMessageParts = message.split("Detalhe: Key ");
            if (errorMessageParts.length > 1) {
                String errorMessage = errorMessageParts[1].replace("(", "").replace(")", "");
                String[] fieldParts = errorMessage.split("=");
                if (fieldParts.length > 1) {
                    String fieldName = fieldParts[0].trim();
                    String fieldValue = fieldParts[1].trim();
                    return new FieldErrorResponse(fieldName, fieldValue);
                }
            }
        }
        return null;
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        String message;

        if (ex.getMessage().startsWith("Invalid or expired token")) {
            message = "Invalid or expired token";
            log.error(message, ex);
            return handleExceptionInternal(ex, message, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
        } else {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        Throwable cause = ex.getCause();
        if (cause instanceof DateTimeParseException || cause instanceof JsonMappingException) {
            String message = "The expected format for date is: dd/MM/yyyy";
            log.error(message, ex);
            ErrorsResponse errorResponse = new ErrorsResponse(HttpStatus.BAD_REQUEST.value(), "Invalid date format.", List.of(new FieldErrorResponse("dateOfBirth", message)));
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } else {
            log.error(ex.getMessage(), ex);
            return super.handleHttpMessageNotReadable(ex, headers, status, request);
        }
    }

    @ExceptionHandler(value = {AuthenticationFailedException.class})
    protected ResponseEntity<Object> handleAuthenticationFailedException(AuthenticationFailedException ex) {
        String message = "Invalid email or password. Please try again.";
        log.error(message, ex);
        ErrorsResponse errorResponse = new ErrorsResponse(HttpStatus.UNAUTHORIZED.value(), message);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {PasswordResetEmailException.class})
    protected ResponseEntity<Object> handlePasswordResetEmailException(PasswordResetEmailException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {PasswordRedefinitionBlockNotExpiredException.class})
    protected ResponseEntity<Object> handlePasswordRedefinitionBlockNotExpiredException(PasswordRedefinitionBlockNotExpiredException ex) {
        log.error(ex.getMessage(), ex);
        ErrorsResponse errorResponse = new ErrorsResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {PasswordResetInvalidTokenException.class})
    protected ResponseEntity<Object> handlePasswordResetInvalidTokenException(PasswordResetInvalidTokenException ex) {
        log.error(ex.getMessage(), ex);
        ErrorsResponse errorResponse = new ErrorsResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token.");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorsResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        List<FieldErrorResponse> errors = ex.getConstraintViolations().stream()
                .filter(violation -> violation != null && violation.getPropertyPath() != null)
                .map(violation -> new FieldErrorResponse(getFieldName(violation.getPropertyPath()), violation.getMessage()))
                .toList();
        ErrorsResponse customEx = new ErrorsResponse(HttpStatus.BAD_REQUEST.value(), "Invalid field", errors);
        log.error(customEx.toString(), ex);
        return new ResponseEntity<>(customEx, HttpStatus.BAD_REQUEST);
    }

    private String getFieldName(Path path) {
        String[] pathElements = path.toString().split("\\.");
        return pathElements.length > 1 ? pathElements[pathElements.length - 1] : "";
    }

    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<ErrorsResponse> handleUnexpectedTypeException(UnexpectedTypeException ex) {
        ErrorsResponse errorResponse = new ErrorsResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        log.error(errorResponse.toString(), ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordValidationException.class)
    public ResponseEntity<ErrorsResponse> handlePasswordValidationException(PasswordValidationException ex) {
        ErrorsResponse errorResponse = new ErrorsResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        log.error(errorResponse.toString(), ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        List<FieldErrorResponse> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();
        ErrorsResponse errorResponse = new ErrorsResponse(status.value(), "Your request could not be processed due to an error in the data you submitted.", errors);
        log.error(errorResponse.toString(), ex);
        return new ResponseEntity<>(errorResponse, headers, status);
    }
}