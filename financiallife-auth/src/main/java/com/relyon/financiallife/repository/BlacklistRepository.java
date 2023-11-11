package com.relyon.financiallife.repository;

import com.relyon.financiallife.model.authentication.revocation.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {
    boolean existsByToken(String token);
}