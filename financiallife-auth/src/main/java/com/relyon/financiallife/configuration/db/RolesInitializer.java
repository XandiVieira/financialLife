package com.relyon.financiallife.configuration.db;


import com.relyon.financiallife.mapper.RoleMapper;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.dto.RoleRequest;
import com.relyon.financiallife.repository.PermissionRepository;
import com.relyon.financiallife.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
@Slf4j
@Order(2)
public class RolesInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    public void run(String... args) {
        createDefaultRoles();
    }

    private void createDefaultRoles() {
        log.info("Creating default roles...");
        List<Role> roles = buildRoles(permissionRepository.findAll());
        roleRepository.saveAll(roles);
        log.info("Default roles created.");
    }

    private List<Role> buildRoles(List<Permission> defaultPermissions) {
        log.info("Building roles...");

        List<String> managerPermissionsToExclude = List.of("permission:create", "permission:update", "permission:delete");

        List<Role> roles = Stream.of(
                        new RoleRequest("admin"),
                        new RoleRequest("manager"),
                        new RoleRequest("user"))
                .map((RoleRequest request) -> roleMapper.roleRequestToRoleModel(request, Collections.emptyList()))
                .filter(role -> !roleRepository.existsByName(role.getName()))
                .toList();
        roles.forEach(role -> {
            if (role.getName().equalsIgnoreCase("ROLE_ADMIN")) {
                role.setPermissions(defaultPermissions);
            } else if (role.getName().equalsIgnoreCase("ROLE_MANAGER")) {
                role.setPermissions(defaultPermissions.stream().filter(permission -> !managerPermissionsToExclude.contains(permission.getName())).toList());
            }
            role.setCreatedBy("alexandre.vieira@relyon.dev.br");
            role.setCreatedAt(LocalDateTime.now());
        });
        return roles;
    }
}