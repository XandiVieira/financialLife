package com.relyon.financiallife.model.password;

import com.relyon.financiallife.model.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public PasswordResetToken(String token, User user, int expirationTimeInMinutes) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(expirationTimeInMinutes);
    }

    private LocalDateTime calculateExpiryDate(int expirationTimeInMinutes) {
        return LocalDateTime.now().plusMinutes(expirationTimeInMinutes);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}