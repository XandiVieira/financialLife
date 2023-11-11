package com.relyon.financiallife.service;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.permission.PermissionFilters;
import com.relyon.financiallife.exception.custom.AuthenticationFailedException;
import com.relyon.financiallife.exception.custom.BadRequestException;
import com.relyon.financiallife.exception.custom.ForbiddenException;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.enums.PermissionEnum;
import com.relyon.financiallife.model.permissions.enums.ProtectedPermissionEnum;
import com.relyon.financiallife.repository.PermissionRepository;
import com.relyon.financiallife.repository.specification.PermissionSpecification;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public List<Permission> createPermissions(List<Permission> permissionsRequest) {
        log.info("Creating permissions: {}", permissionsRequest);
        if (ObjectUtils.isEmpty(permissionsRequest)) {
            throw new BadRequestException("At least one permission must be informed");
        }
        List<Permission> permissionsCreated = permissionRepository.saveAll(permissionsRequest);
        log.info("Permissions created successfully: {}", permissionsCreated);
        return permissionsCreated;
    }

    public Page<Permission> getAllPermissions(Pagination pagination, PermissionFilters permissionFilters, BaseSort baseSort) {
        log.info("Getting all permissions with pagination: {}, filters: {}, sort: {}", pagination, permissionFilters, baseSort);

        Specification<Permission> permissionSpecification = buildPermissionSpecification(permissionFilters);
        Pageable pageable = buildPagination(pagination, baseSort);

        Page<Permission> permissionsPage = permissionRepository.findAll(permissionSpecification, pageable);
        if (!isAdmin()) {
            permissionsPage = new PageImpl<>(permissionsPage.stream().filter(permission -> !Arrays.stream(ProtectedPermissionEnum.values()).map(ProtectedPermissionEnum::getPermissionName).toList().contains(permission.getName())).toList());
        }
        log.info("Found {} permissions: {}", permissionsPage.getTotalElements(), permissionsPage.getContent());
        return permissionsPage;
    }

    public List<Permission> getAllPermissionsByIds(List<Integer> permissionIds) {
        log.info("Getting all permissions by IDs: {}", permissionIds);
        if (permissionIds == null || permissionIds.isEmpty()) {
            log.warn("Empty permission ID list provided");
            throw new BadRequestException("At least one permission must be informed");
        }

        List<Permission> returnedPermissions;
        if (isAdmin()) {
            returnedPermissions = permissionRepository.findAllById(permissionIds);
            if (returnedPermissions.isEmpty()) {
                log.error("No permissions found for IDs: {}", permissionIds);
                throw new NotFoundException("Permission(s) not found.");
            }
        } else {
            returnedPermissions = permissionRepository.findByIdsByRoleManager(permissionIds);
            if (permissionIds.size() > returnedPermissions.size()) {
                throw new ForbiddenException("Some permission(s) are not allowed");
            }
        }

        log.info("Found permissions: {}", returnedPermissions);
        return returnedPermissions;
    }

    public Permission updatePermission(Integer id, Permission permissionRequest) {
        log.info("Updating permission with id: {}, permission: {}", id, permissionRequest);
        Permission existingPermission = getPermission(id);
        if (Arrays.stream(PermissionEnum.values()).map(PermissionEnum::getPermissionName).toList().contains(existingPermission.getName())) {
            throw new ForbiddenException("This permission cannot be altered");
        }
        permissionRequest.setId(existingPermission.getId());
        Permission updatedPermission = permissionRepository.save(permissionRequest);
        log.info("Permission updated successfully: {}", updatedPermission);
        return updatedPermission;
    }

    public void deletePermission(Integer id) {
        log.info("Deleting permission with id: {}", id);
        Permission existingPermission = getPermission(id);
        if (Arrays.stream(PermissionEnum.values()).map(PermissionEnum::getPermissionName).toList().contains(existingPermission.getName())) {
            throw new ForbiddenException("This permission cannot be deleted");
        }
        permissionRepository.deleteById(id);
        log.info("Permission deleted successfully: {}", existingPermission);
    }

    private Permission getPermission(Integer id) {
        log.info("Getting permission with id: {}", id);
        return permissionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Permission with id {} not found", id);
                    return new NotFoundException("Permission with id " + id + " was not found.");
                });
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

    private static Specification<Permission> buildPermissionSpecification(PermissionFilters permissionFilters) {
        return PermissionSpecification.builder()
                .name(permissionFilters.getName())
                .createdBy(permissionFilters.getCreatedBy())
                .lastModifiedBy(permissionFilters.getLastModifiedBy())
                .build();
    }
}