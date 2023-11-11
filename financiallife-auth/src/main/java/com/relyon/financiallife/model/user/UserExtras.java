package com.relyon.financiallife.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_extras")
@Builder
public class UserExtras {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime lastLogin;

    @Column
    private int loginAttempts = 0;

    @Column
    private int passwordRedefinitionAttempts = 0;

    @Column
    private LocalDateTime passwordRedefinitionBlockExpirationTime = LocalDateTime.now();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void incrementLoginAttempts() {
        loginAttempts++;
    }

    public void handlePasswordRedefinitionAttempt() {
        passwordRedefinitionAttempts++;
        passwordRedefinitionBlockExpirationTime = LocalDateTime.now().plusMinutes(passwordRedefinitionAttempts);
    }

    public void resetLoginAttempts() {
        if (loginAttempts != 0) {
            loginAttempts = 0;
        }
    }

    public void resetPasswordRedefinitionAttempt() {
        if (passwordRedefinitionAttempts != 0) {
            passwordRedefinitionAttempts = 0;
        }
    }

    @Override
    public String toString() {
        return "UserExtras{" +
                "id=" + id +
                "userId=" + user.getId() +
                ", lastLogin=" + lastLogin +
                ", loginAttempts=" + loginAttempts +
                ", passwordRedefinitionAttempts=" + passwordRedefinitionAttempts +
                ", passwordRedefinitionBlockExpirationTime=" + passwordRedefinitionBlockExpirationTime +
                '}';
    }
}