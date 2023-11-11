package com.relyon.financiallife.repository;

import com.relyon.financiallife.model.user.UserExtras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserExtrasRepository extends JpaRepository<UserExtras, Long> {
    UserExtras findByUserId(Long userId);

    void deleteByUserId(Long userId);
}