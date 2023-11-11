package com.relyon.financiallife.service;

import com.relyon.financiallife.configuration.security.JwtService;
import com.relyon.financiallife.exception.custom.AccountDisabledException;
import com.relyon.financiallife.exception.custom.AccountLockedException;
import com.relyon.financiallife.model.authentication.dto.AuthenticationRequest;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.UserExtras;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.webjars.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void authenticate_WithValidCredentials_ShouldReturn200() {
        String email = "test@test.com";
        String password = "password123";
        User user = createUser();
        AuthenticationRequest request = new AuthenticationRequest(email, password);

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("test_token");

        ResponseEntity<?> response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldReturn401() {
        String email = "test@test.com";
        String password = "password123";
        AuthenticationRequest request = new AuthenticationRequest(email, password);

        when(userService.getUserByEmail(email)).thenReturn(createUser());
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException(""));

        ResponseEntity<?> response = authenticationService.authenticate(request);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void authenticate_WithLockedUser_ShouldThrowAccountLockedException() {
        String email = "test@test.com";
        String password = "password123";
        AuthenticationRequest request = new AuthenticationRequest(email, password);

        User user = createUser();
        user.setNonLocked(false);
        when(userService.getUserByEmail(email)).thenReturn(user);

        Exception exception = assertThrows(AccountLockedException.class, () -> authenticationService.authenticate(request));

        assertEquals("Your account is locked. Please contact support.", exception.getMessage());
    }

    @Test
    void authenticate_WithNonEnabledUser_ShouldThrowAccountDisabledException() {
        String email = "test@test.com";
        String password = "password123";
        AuthenticationRequest request = new AuthenticationRequest(email, password);

        User user = createUser();
        user.setEnabled(false);
        when(userService.getUserByEmail(email)).thenReturn(user);

        Exception exception = assertThrows(AccountDisabledException.class, () -> authenticationService.authenticate(request));

        assertEquals("Enable your account by resetting the password.", exception.getMessage());
    }

    @Test
    void authenticate_WithInvalidCredentialsAndLoginAttemptsAtMax_ShouldThrowAccountLockedException() {
        String email = "test@test.com";
        String password = "password123";
        AuthenticationRequest request = new AuthenticationRequest(email, password);

        User user = createUser();
        user.getUserExtras().setLoginAttempts(5);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException(""));

        Exception exception = assertThrows(AccountLockedException.class, () -> authenticationService.authenticate(request));

        assertEquals("Your account was locked due to too many failed login attempts. Please contact support.", exception.getMessage());
    }

    @Test
    void authenticate_WithNonExistingUser_ShouldThrowUserNotFound() {
        String email = "test@test.com";
        String password = "password123";
        AuthenticationRequest request = new AuthenticationRequest(email, password);

        when(userService.getUserByEmail(email)).thenThrow(new NotFoundException("Email test@test.com not found."));

        Exception exception = assertThrows(NotFoundException.class, () -> authenticationService.authenticate(request));

        assertEquals("Email test@test.com not found.", exception.getMessage());
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .cpf("123456789")
                .cellphoneNumber("+1 555-555-5555")
                .email("johndoe@example.com")
                .password("password123")
                .enabled(true)
                .userExtras(UserExtras.builder().loginAttempts(0).lastLogin(LocalDateTime.now()).passwordRedefinitionAttempts(0).passwordRedefinitionBlockExpirationTime(LocalDateTime.now()).build())
                .isNonExpired(true)
                .isNonLocked(true)
                .isCredentialsNonExpired(true)
                .roles(Collections.singletonList(Role.builder().name("ROLE_USER").build()))
                .build();
    }
}