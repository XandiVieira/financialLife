package com.relyon.financiallife.repository;

import com.relyon.financiallife.model.password.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findAllByUserId(Long userId);

    void deleteByUserId(Long userId);
}