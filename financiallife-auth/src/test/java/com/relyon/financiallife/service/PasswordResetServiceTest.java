package com.relyon.financiallife.service;

import com.relyon.financiallife.exception.custom.PasswordRedefinitionBlockNotExpiredException;
import com.relyon.financiallife.exception.custom.PasswordResetEmailException;
import com.relyon.financiallife.exception.custom.PasswordResetInvalidTokenException;
import com.relyon.financiallife.exception.custom.PasswordValidationException;
import com.relyon.financiallife.model.password.PasswordHistory;
import com.relyon.financiallife.model.password.PasswordResetToken;
import com.relyon.financiallife.model.password.dto.PasswordResetEmailResponse;
import com.relyon.financiallife.model.password.dto.PasswordResetResponse;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.UserExtras;
import com.relyon.financiallife.repository.PasswordHistoryRepository;
import com.relyon.financiallife.repository.PasswordResetTokenRepository;
import com.relyon.financiallife.repository.UserExtrasRepository;
import com.relyon.financiallife.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserExtrasRepository userExtrasRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void testSendPasswordResetEmail_ShouldReturn200() throws Exception {
        User user = createUser();
        PasswordResetToken token = new PasswordResetToken(UUID.randomUUID().toString(), user, 5);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(token);

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        doAnswer(invocation -> {
            completableFuture.complete(null);
            return null;
        }).when(mailSender).send(messageCaptor.capture());

        ResponseEntity<PasswordResetEmailResponse> response = passwordResetService.sendPasswordResetEmail(user.getEmail());

        completableFuture.get();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertEquals(mimeMessage, messageCaptor.getValue());
    }


    @Test
    void sendPasswordResetEmail_WithPasswordRedefinitionBlockNonExpired_ShouldThrowPasswordRedefinitionBlockNotExpiredException() {
        User user = createUser();
        user.getUserExtras().setPasswordRedefinitionBlockExpirationTime(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        verify(mailSender, times(0)).send(any(MimeMessage.class));

        assertThrows(PasswordRedefinitionBlockNotExpiredException.class, () -> passwordResetService.sendPasswordResetEmail("test@example.com"));
    }

    @Test
    void testSendWelcomeEmail_ShouldReturn200() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        doAnswer(invocation -> {
            completableFuture.complete(null);
            return null;
        }).when(mailSender).send(messageCaptor.capture());

        passwordResetService.sendWelcomeEmail(user.getEmail(), user.getPassword());

        completableFuture.get();

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertEquals(mimeMessage, messageCaptor.getValue());
    }


    @Test
    @Disabled(value = "The test does not work because of CompletableFuture.runAsync")
        //TODO fix test to simulate exception throwing using CompletableFuture.runAsync
    void sendWelcomeEmail_WithMessagingException_ShouldThrowPasswordResetEmailException() {
        User user = new User();
        user.setEmail("email@email.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        doAnswer(invocation -> {
            throw new MessagingException();
        }).when(mailSender).send(any(MimeMessage.class));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThrows(PasswordResetEmailException.class, () -> passwordResetService.sendWelcomeEmail("test@example.com", user.getPassword()));
    }

    @Test
    void resetPassword_ShouldReturn200() {
        String tokenValue = "abc123";
        String password = "newPassword@123";

        User user = createUser();
        user.setPassword("oldPassword@123");

        PasswordResetToken token = new PasswordResetToken(UUID.randomUUID().toString(), user, 5);

        when(passwordResetTokenRepository.findByToken(tokenValue)).thenReturn(token);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        ResponseEntity<PasswordResetResponse> response = passwordResetService.resetPassword(tokenValue, password, password);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successful", response.getBody().getMessage());
        verify(passwordResetTokenRepository, times(1)).deleteByUserId(user.getId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void resetPassword_WithAlreadyUsedPassword_ShouldThrowPasswordValidationException() {
        String tokenValue = "abc123";
        String password = "newPassword@12355";

        User user = createUser();

        PasswordResetToken token = new PasswordResetToken(UUID.randomUUID().toString(), user, 5);

        when(passwordHistoryRepository.findAllByUserId(anyLong())).thenReturn(user.getPasswordHistory());
        when(passwordResetTokenRepository.findByToken(tokenValue)).thenReturn(token);
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        assertThrows(PasswordValidationException.class, () -> passwordResetService.resetPassword(tokenValue, password, password));

        verify(userRepository, never()).save(any());
    }


    @Test
    void resetPassword_WithEmptyTokenValueAndPassword_ShouldReturnBadRequest() {
        ResponseEntity<PasswordResetResponse> response = passwordResetService.resetPassword("", "", "");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Token value and password cannot be empty", response.getBody().getMessage());
    }

    @Test
    void resetPassword_WithNullToken_ShouldThrowPasswordResetInvalidTokenException() {
        String tokenValue = "abc123";
        String password = "newPassword";

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(null);

        assertThrows(PasswordResetInvalidTokenException.class, () -> passwordResetService.resetPassword(tokenValue, password, password));
    }

    @Test
    @Disabled(value = "The test does not work because of CompletableFuture.runAsync")
        //TODO fix test to simulate exception throwing using CompletableFuture.runAsync
    void sendPasswordResetEmail_WithMessagingException_ShouldThrowPasswordResetEmailException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(createUser()));
        doAnswer(invocation -> {
            throw new MessagingException();
        }).when(mailSender).send(any(MimeMessage.class));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        PasswordResetEmailException exception = assertThrows(PasswordResetEmailException.class, () -> passwordResetService.sendPasswordResetEmail("test@example.com"));

        assertEquals("Failed to send password reset email, please try again later.", exception.getMessage());
    }

    private User createUser() {
        return User.builder().id(1L).firstName("John").lastName("Doe").username("johndoe").dateOfBirth(LocalDate.of(1990, 1, 1))
                .cpf("123456789").cellphoneNumber("+1 555-555-5555").email("johndoe@example.com").password("Password@12355").enabled(true).isNonExpired(true)
                .userExtras(UserExtras.builder().loginAttempts(0).lastLogin(LocalDateTime.now()).passwordRedefinitionAttempts(0).passwordRedefinitionBlockExpirationTime(LocalDateTime.now()).build())
                .passwordHistory(Collections.singletonList(new PasswordHistory("Password@12355")))
                .isNonLocked(true).isCredentialsNonExpired(true).roles(Collections.singletonList(Role.builder().name("ROLE_USER").build())).build();
    }
}