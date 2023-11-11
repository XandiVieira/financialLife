package com.relyon.financiallife.configuration.db;


import com.relyon.financiallife.mapper.PermissionMapper;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.dto.PermissionRequest;
import com.relyon.financiallife.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
@Order(1)
public class PermissionsInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public void run(String... args) {
        createDefaultPermissions();
    }

    private void createDefaultPermissions() {
        log.info("Creating default permissions...");
        List<Permission> permissions = buildPermissions();
        log.info("Default permissions created.");
        permissionRepository.saveAll(permissions);
    }

    private List<Permission> buildPermissions() {
        List<String> entities = Arrays.asList("user", "role", "permission");
        List<String> operations = Arrays.asList("create", "view", "update", "delete");

        List<Permission> permissions = entities.stream()
                .flatMap(entity -> operations.stream()
                        .map(operation -> {
                            String resourceName = entity + ":" + operation;
                            PermissionRequest permissionRequest = new PermissionRequest(entity, operation);
                            Permission permission = permissionMapper.permissionRequestToPermissionModel(permissionRequest);
                            permission.setName(resourceName);
                            return permission;
                        }))
                .filter(permission -> !permissionRepository.existsByName(permission.getName())).toList();

        String createdBy = "alexandre.vieira@relyon.dev.br";
        LocalDateTime createdAt = LocalDateTime.now();
        permissions.forEach(permission -> {
            permission.setCreatedBy(createdBy);
            permission.setCreatedAt(createdAt);
        });

        return permissions;
    }
}