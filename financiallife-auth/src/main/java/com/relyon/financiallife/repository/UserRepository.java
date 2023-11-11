package com.relyon.financiallife.repository;

import com.relyon.financiallife.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id = :id AND NOT EXISTS (SELECT r FROM Role r WHERE r.name = 'ROLE_ADMIN' AND r MEMBER OF u.roles)")
    Optional<User> findByIdWithoutAdmins(@Param("id") Long id);

    int countUsersByRolesName(String roleAdmin);
}