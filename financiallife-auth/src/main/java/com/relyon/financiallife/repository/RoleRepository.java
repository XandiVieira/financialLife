package com.relyon.financiallife.repository;

import com.relyon.financiallife.model.role.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    boolean existsByName(String name);

    Page<Role> findAll(Specification<Role> roleSpecification, Pageable pageable);

    List<Role> findAllByNameIn(List<String> names);

    @Query("SELECT r FROM Role r WHERE r.name <> 'ROLE_ADMIN' AND r.id IN :ids")
    List<Role> findByIdsExceptAdmin(@Param("ids") List<Integer> ids);
}