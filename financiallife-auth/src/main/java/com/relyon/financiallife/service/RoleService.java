package com.relyon.financiallife.service;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.role.RoleFilters;
import com.relyon.financiallife.exception.custom.AuthenticationFailedException;
import com.relyon.financiallife.exception.custom.BadRequestException;
import com.relyon.financiallife.exception.custom.ForbiddenException;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.enums.ProtectedPermissionEnum;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.enums.RoleEnum;
import com.relyon.financiallife.repository.RoleRepository;
import com.relyon.financiallife.repository.specification.RoleSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.webjars.NotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    public List<Role> createRoles(List<Role> rolesRequest) {
        log.info("Creating roles: {}", rolesRequest);
        if (ObjectUtils.isEmpty(rolesRequest)) {
            throw new BadRequestException("At least one role must be informed");
        }
        rolesRequest.forEach(role -> role.getPermissions().forEach(permission -> {
            if (Arrays.stream(ProtectedPermissionEnum.values()).map(ProtectedPermissionEnum::getPermissionName).toList().contains(permission.getName())) {
                throw new ForbiddenException("Some permission(s) are not allowed for this role");
            }
        }));
        List<Role> rolesCreated = roleRepository.saveAll(rolesRequest);
        log.info("Roles created successfully: {}", rolesCreated);
        return rolesCreated;
    }

    public Page<Role> getAllRoles(Pagination pagination, RoleFilters roleFilters, BaseSort baseSort) {
        log.info("Getting all roles with pagination: {}, filters: {}, sort: {}", pagination, roleFilters, baseSort);

        Specification<Role> roleSpecification = buildRoleSpecification(roleFilters);
        Pageable pageable = buildPagination(pagination, baseSort);

        Page<Role> rolesPage = roleRepository.findAll(roleSpecification, pageable);

        if (!isAdmin()) {
            rolesPage = new PageImpl<>(rolesPage.stream().filter(role -> !role.getName().equalsIgnoreCase("ROLE_ADMIN")).toList());
        }

        log.info("Found {} roles: {}", rolesPage.getTotalElements(), rolesPage.getContent());
        return rolesPage;
    }

    public List<Role> getAllRolesByIds(List<Integer> roleIds) {
        log.info("Getting all roles by IDs: {}", roleIds);
        if (roleIds == null || roleIds.isEmpty()) {
            log.warn("Empty role ID list provided");
            throw new BadRequestException("At least one role must be informed");
        }

        List<Role> returnedRoles;
        if (isAdmin()) {
            returnedRoles = roleRepository.findAllById(roleIds);
            if (returnedRoles.isEmpty()) {
                log.error("No roles found for IDs: {}", roleIds);
                throw new NotFoundException("Role(s) not found.");
            }
        } else {
            returnedRoles = roleRepository.findByIdsExceptAdmin(roleIds);
            if (roleIds.size() > returnedRoles.size()) {
                throw new ForbiddenException("Some role(s) are not allowed");
            }
        }

        log.info("Found roles: {}", returnedRoles);
        return returnedRoles;
    }

    public Role updateRole(Integer id, Role roleRequest) {
        log.info("Updating role with id: {}, role: {}", id, roleRequest);
        Role existingRole = getRole(id);
        roleRequest.setId(existingRole.getId());
        if (Arrays.stream(RoleEnum.values()).map(Objects::toString).toList().contains(existingRole.getName())) {
            throw new ForbiddenException("This role cannot be altered");
        } else if (Arrays.stream(ProtectedPermissionEnum.values()).map(ProtectedPermissionEnum::getPermissionName).toList().stream().anyMatch(roleRequest.getPermissions().stream().map(Permission::getName).toList()::contains)) {
            throw new ForbiddenException("Some permission(s) are not allowed for this role");
        }
        Role updatedRole = roleRepository.save(roleRequest);
        log.info("Role updated successfully: {}", updatedRole);
        return updatedRole;
    }

    public void deleteRole(Integer id) {
        log.info("Deleting role with id: {}", id);
        Role role = getRole(id);
        if (Arrays.stream(RoleEnum.values()).map(Objects::toString).toList().contains(role.getName())) {
            throw new ForbiddenException("This resource cannot be deleted");
        }
        roleRepository.deleteById(id);
        log.info("Role deleted successfully: {}", role);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AuthenticationFailedException("User not authenticated.");
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
                .contains("ROLE_ADMIN");
    }

    private static PageRequest buildPagination(Pagination pagination, BaseSort baseSort) {
        return PageRequest.of(
                pagination.getPageNumber(),
                pagination.getPageSize(),
                org.springframework.data.domain.Sort.by(getSort(baseSort.getSort()))
        );
    }

    private static List<org.springframework.data.domain.Sort.Order> getSort(String sort) {
        List<org.springframework.data.domain.Sort.Order> orders = new ArrayList<>();
        for (String field : sort.split(",")) {
            if (field.startsWith("-")) {
                orders.add(new org.springframework.data.domain.Sort.Order(org.springframework.data.domain.Sort.Direction.DESC, field.substring(1)));
            } else {
                orders.add(new org.springframework.data.domain.Sort.Order(org.springframework.data.domain.Sort.Direction.ASC, field));
            }
        }
        return orders;
    }

    private static Specification<Role> buildRoleSpecification(RoleFilters roleFilters) {
        return RoleSpecification.builder()
                .name(roleFilters.getName())
                .createdBy(roleFilters.getCreatedBy())
                .lastModifiedBy(roleFilters.getLastModifiedBy())
                .build();
    }

    private Role getRole(Integer id) {
        log.info("Getting role with id: {}", id);
        return roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Role with id {} not found", id);
                    return new NotFoundException("Role with id " + id + " was not found.");
                });
    }
}