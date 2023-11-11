package com.relyon.financiallife.configuration.db;


import com.relyon.financiallife.model.password.PasswordHistory;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.UserExtras;
import com.relyon.financiallife.repository.PasswordHistoryRepository;
import com.relyon.financiallife.repository.RoleRepository;
import com.relyon.financiallife.repository.UserExtrasRepository;
import com.relyon.financiallife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
@Order(3)
public class UsersInitializer implements CommandLineRunner {

    @Value("${initial-user-password}")
    private String initialUserPassword;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final UserExtrasRepository userExtrasRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createSuperUser();
    }

    private void createSuperUser() {
        log.info("Creating super user...");

        User user = buildUser();
        user.setCreatedBy(user.getEmail());
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setLastModifiedAt(now);

        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        User userReturned = optionalUser.orElseGet(() -> userRepository.save(user));

        if (userReturned.getPasswordHistory() == null) {
            passwordHistoryRepository.save(new PasswordHistory(userReturned, userReturned.getPassword()));
        }
        if (userReturned.getUserExtras() == null) {
            userExtrasRepository.save(buildUserExtras(userReturned));
        }

        log.info("Super user created: {}", userReturned);
    }

    private User buildUser() {
        log.info("Building user...");
        return User.builder()
                .firstName("Alexandre")
                .lastName("Vieira")
                .username("alexandre.vieira")
                .dateOfBirth(LocalDate.of(1997, 6, 30))
                .cpf("012.164.370-09")
                .cellphoneNumber("(51) 99751-3229")
                .email("alexandre.vieira@relyon.dev.br")
                .password(passwordEncoder.encode(initialUserPassword))
                .roles(roleRepository.findAllByNameIn(List.of("ROLE_ADMIN")))
                .enabled(true)
                .isNonExpired(true)
                .isNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
    }

    private UserExtras buildUserExtras(User user) {
        return UserExtras.builder().user(user).loginAttempts(0).lastLogin(LocalDateTime.now()).passwordRedefinitionAttempts(0).passwordRedefinitionBlockExpirationTime(LocalDateTime.now()).build();
    }
}