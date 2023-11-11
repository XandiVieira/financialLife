package com.relyon.financiallife.service;

import com.relyon.financiallife.configuration.security.JwtService;
import com.relyon.financiallife.exception.custom.AccountDisabledException;
import com.relyon.financiallife.exception.custom.AccountLockedException;
import com.relyon.financiallife.model.authentication.dto.AuthenticationRequest;
import com.relyon.financiallife.model.authentication.dto.FailedAuthenticationResponse;
import com.relyon.financiallife.model.authentication.dto.SuccessfulAuthenticationResponse;
import com.relyon.financiallife.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final int MAX_LOGIN_ATTEMPTS = 6;

    public ResponseEntity<?> authenticate(AuthenticationRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());

        User user = userService.getUserByEmail(request.getEmail());

        if (!user.isNonLocked()) {
            log.warn("Account locked for user with email: {}", request.getEmail());
            throw new AccountLockedException("Your account is locked. Please contact support.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            log.info("User authenticated successfully");
            handleLoginAttemptsReset(user);

            if (!user.isEnabled()) {
                log.warn("Enable your account by resetting the password.");
                throw new AccountDisabledException("Enable your account by resetting the password.");
            }
        } catch (AuthenticationException e) {
            handleFailedLogin(user);
            int remainingAttempts = MAX_LOGIN_ATTEMPTS - user.getUserExtras().getLoginAttempts();
            FailedAuthenticationResponse failedResponse = FailedAuthenticationResponse.builder()
                    .errorMessage("Invalid email or password. Please try again. Remaining attempts: " + remainingAttempts)
                    .remainingAttempts(remainingAttempts)
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(failedResponse);
        }

        var jwtToken = jwtService.generateToken(user);
        log.info("JWT token generated for user with email: {}", request.getEmail());

        userService.updateLastLogin(user);

        log.info("Returning authentication response for user with email: {}", request.getEmail());
        return ResponseEntity.ok(SuccessfulAuthenticationResponse.builder()
                .token(jwtToken)
                .build());
    }

    private void handleLoginAttemptsReset(User user) {
        user.getUserExtras().resetLoginAttempts();
        userService.updateLoginAttempts(user);
        log.info("User login attempts reset.");
    }

    private void handleFailedLogin(User user) {
        user.getUserExtras().incrementLoginAttempts();

        if (user.getUserExtras().getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            user.setNonLocked(false);
            userService.updateLoginAttempts(user);
            log.warn("User account locked due to too many failed login attempts: {}", user.getEmail());
            throw new AccountLockedException("Your account was locked due to too many failed login attempts. Please contact support.");
        } else {
            userService.updateLoginAttempts(user);
            log.info("User login attempts incremented to {}", user.getUserExtras().getLoginAttempts());
        }
    }
}