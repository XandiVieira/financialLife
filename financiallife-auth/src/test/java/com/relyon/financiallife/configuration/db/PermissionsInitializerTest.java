package com.relyon.financiallife.configuration.db;

import com.relyon.financiallife.mapper.PermissionMapper;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.dto.PermissionRequest;
import com.relyon.financiallife.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionsInitializerTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionsInitializer permissionsInitializer;

    @Test
    void run_ShouldNotCreateDefaultPermissions() {
        List<String> entities = Arrays.asList("user", "role", "permission");
        List<String> operations = Arrays.asList("create", "view", "update", "delete");

        entities.forEach(entity -> operations.forEach(operation -> {
            String resourceName = entity + ":" + operation;
            PermissionRequest permissionRequest = new PermissionRequest(entity, operation);
            when(permissionMapper.permissionRequestToPermissionModel(permissionRequest))
                    .thenReturn(new Permission(resourceName));
            when(permissionRepository.existsByName(resourceName)).thenReturn(true);
        }));

        permissionsInitializer.run();

        verify(permissionRepository, times(1)).saveAll(Collections.emptyList());
    }

    @Test
    void run_ShouldCreateDefaultPermissions() {
        List<String> entities = Arrays.asList("user", "role", "permission");
        List<String> operations = Arrays.asList("create", "view", "update", "delete");

        entities.forEach(entity -> operations.forEach(operation -> {
            String resourceName = entity + ":" + operation;
            PermissionRequest permissionRequest = new PermissionRequest(entity, operation);
            when(permissionMapper.permissionRequestToPermissionModel(permissionRequest))
                    .thenReturn(new Permission(resourceName));
            when(permissionRepository.existsByName(resourceName)).thenReturn(false);
        }));

        permissionsInitializer.run();

        verify(permissionRepository, times(1)).saveAll(anyList());
    }
}