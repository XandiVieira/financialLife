package com.relyon.financiallife.repository;

import com.relyon.financiallife.model.permissions.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    boolean existsByName(String name);

    Page<Permission> findAll(Specification<Permission> permissionSpecification, Pageable pageable);

    @Query("SELECT p FROM Role r JOIN r.permissions p WHERE r.name = 'ROLE_MANAGER' AND p.id IN :ids")
    List<Permission> findByIdsByRoleManager(@Param("ids") List<Integer> ids);
}