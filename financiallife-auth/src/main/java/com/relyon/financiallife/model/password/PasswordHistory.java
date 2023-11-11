package com.relyon.financiallife.model.password;

import com.relyon.financiallife.model.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Table(name = "password_history")
@Entity
@Data
public class PasswordHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String password;

    public PasswordHistory(User user, String password) {
        this.user = user;
        this.password = password;
    }

    public PasswordHistory(String password) {
        this.password = password;
    }
}