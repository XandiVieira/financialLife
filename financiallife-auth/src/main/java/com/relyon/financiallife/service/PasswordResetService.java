package com.relyon.financiallife.service;

import com.relyon.financiallife.exception.custom.PasswordRedefinitionBlockNotExpiredException;
import com.relyon.financiallife.exception.custom.PasswordResetEmailException;
import com.relyon.financiallife.exception.custom.PasswordResetInvalidTokenException;
import com.relyon.financiallife.exception.custom.PasswordValidationException;
import com.relyon.financiallife.model.password.PasswordHistory;
import com.relyon.financiallife.model.password.PasswordResetToken;
import com.relyon.financiallife.model.password.dto.PasswordResetEmailResponse;
import com.relyon.financiallife.model.password.dto.PasswordResetResponse;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.UserExtras;
import com.relyon.financiallife.repository.PasswordHistoryRepository;
import com.relyon.financiallife.repository.PasswordResetTokenRepository;
import com.relyon.financiallife.repository.UserExtrasRepository;
import com.relyon.financiallife.repository.UserRepository;
import com.relyon.financiallife.utils.PasswordValidator;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    @Value("${mail.username}")
    private String emailFrom;
    @Value("${client.name}")
    private String clientName;
    @Value("${base-url}")
    private String passwordResetBaseUrl;
    @Value("${server.port}")
    private String port;
    @Value("${password-reset.token-expiration-time}")
    private int expirationTimeInMinutes;

    private final UserRepository userRepository;
    private final UserExtrasRepository userExtrasRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<PasswordResetEmailResponse> sendPasswordResetEmail(String email) {
        log.info("Sending password reset email...");
        User user = findUserByEmail(email);
        UserExtras userExtras = user.getUserExtras();

        if (isPasswordRedefinitionBlockExpired(userExtras)) {
            try {
                PasswordResetToken token = generateToken(user);
                userExtras.handlePasswordRedefinitionAttempt();
                userExtrasRepository.save(userExtras);

                sendEmail(buildPasswordResetEmailMessage(user, token));

                log.info("Password reset email sent to user {}", user.getEmail());
                return ResponseEntity.ok(new PasswordResetEmailResponse("Password reset email sent successfully", userExtras.getPasswordRedefinitionBlockExpirationTime()));
            } catch (MessagingException e) {
                log.error("Failed to send password reset email to user {}", user.getEmail(), e);
                throw new PasswordResetEmailException("Failed to send password reset email, please try again later.");
            }
        } else {
            LocalDateTime blockExpirationTime = userExtras.getPasswordRedefinitionBlockExpirationTime();
            Duration remainingDuration = Duration.between(LocalDateTime.now(), blockExpirationTime);
            long remainingMinutes = remainingDuration.toMinutes();
            throw new PasswordRedefinitionBlockNotExpiredException("You will be able to send another password redefinition email in: " + remainingMinutes + " minutes.");
        }
    }

    private boolean isPasswordRedefinitionBlockExpired(UserExtras userExtras) {
        LocalDateTime blockExpirationTime = userExtras.getPasswordRedefinitionBlockExpirationTime();
        return LocalDateTime.now().isAfter(blockExpirationTime);
    }

    public void sendWelcomeEmail(String email, String generatedPassword) {
        log.info("Sending welcome email...");
        User user = findUserByEmail(email);

        try {
            sendEmail(buildWelcomeEmailMessage(user, generatedPassword));
            log.info("Welcome email sent to user {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to user {}. The user must reset their password.", user.getEmail(), e);
            throw new PasswordResetEmailException("Failed to send welcome email. The user must reset their password.");
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private PasswordResetToken generateToken(User user) {
        log.info("Generating token...");
        PasswordResetToken token = new PasswordResetToken(UUID.randomUUID().toString(), user, expirationTimeInMinutes);
        passwordResetTokenRepository.save(token);
        return token;
    }

    private void sendEmail(MimeMessage message) {
        CompletableFuture.runAsync(() -> mailSender.send(message));
    }

    private MimeMessage buildPasswordResetEmailMessage(User user, PasswordResetToken passwordResetToken) throws MessagingException {
        log.info("Building password reset email message for user with email {}", user.getEmail());
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setFrom(clientName + " <" + emailFrom + ">");
        helper.setTo(user.getEmail());
        helper.setSubject("Redefina sua senha");
        String name = user.getFirstName() + " " + user.getLastName();
        String resetLink = passwordResetBaseUrl + port + "/api/v1/password-reset/reset?token=" + passwordResetToken.getToken();

        String emailText = "<html><body>"
                + "Prezado(a) " + name + ",<br/><br/>"
                + "Você solicitou a redefinição de senha da sua conta. Por favor, clique no seguinte link para redefinir a senha:<br/><br/>"
                + "<a href=" + resetLink + ">Clique aqui</a><br/><br/>"
                + "Se você não solicitou esta mudança, ignore este email e sua senha permanecerá inalterada.<br/><br/>"
                + "Atenciosamente,<br/>"
                + clientName + " " + passwordResetToken.getToken()
                + "</body></html>";

        helper.setText(emailText, true);

        log.info("Password reset email message built successfully for user with email {}", user.getEmail());
        return message;
    }

    private MimeMessage buildWelcomeEmailMessage(User user, String generatedPassword) throws MessagingException {
        log.info("Building welcome email message for user with email {}", user.getEmail());
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setFrom(clientName + " <" + emailFrom + ">");
        helper.setTo(user.getEmail());
        helper.setSubject("Bem-vindo a " + clientName);
        String name = user.getFirstName() + " " + user.getLastName();

        String emailText = "<html><body>"
                + "Olá, " + name + ".<br/><br/>"
                + "Sua conta foi criada com sucesso em " + clientName + ".<br/><br/>"
                + "Aqui está a sua senha gerada automaticamente:<br/><br/>"
                + generatedPassword + "<br/><br/>"
                + "Faça login utilizando esta senha e altere-a assim que possível.<br/><br/>"
                + "Atenciosamente,<br/>"
                + clientName
                + "</body></html>";

        helper.setText(emailText, true);

        log.info("Welcome email message built successfully for user with email {}", user.getEmail());
        return message;
    }

    @Transactional
    public ResponseEntity<PasswordResetResponse> resetPassword(String tokenValue, String password, String passwordConfirmation) {
        if (StringUtils.isEmpty(tokenValue) || StringUtils.isEmpty(password)) {
            return ResponseEntity.badRequest().body(new PasswordResetResponse("Token value and password cannot be empty"));
        }
        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue);

        if (token == null || token.isExpired()) {
            log.warn("Invalid or expired password reset token");
            throw new PasswordResetInvalidTokenException("Invalid or expired token");
        }

        User user = token.getUser();
        PasswordValidator.validate(password, passwordConfirmation, user);
        user.setPassword(passwordEncoder.encode(password));

        validatePasswordNotPreviouslyUsed(user, password);

        user.setNonLocked(true);
        user.getUserExtras().resetLoginAttempts();
        user.setEnabled(true);
        user.getUserExtras().resetPasswordRedefinitionAttempt();
        userRepository.save(user);

        passwordHistoryRepository.save(new PasswordHistory(user, user.getPassword()));

        passwordResetTokenRepository.deleteByUserId(user.getId());

        log.info("User {} has successfully reset their password", user.getEmail());
        return ResponseEntity.ok(new PasswordResetResponse("Password reset successful"));
    }

    private void validatePasswordNotPreviouslyUsed(User user, String newPassword) throws PasswordValidationException {
        List<String> previousPasswordHashes = passwordHistoryRepository.findAllByUserId(user.getId()).stream()
                .map(PasswordHistory::getPassword).toList();

        if (previousPasswordHashes.stream().anyMatch(previousPasswordHash -> passwordEncoder.matches(newPassword, previousPasswordHash))) {
            throw new PasswordValidationException("Password has been previously used and cannot be reused.");
        }
    }
}