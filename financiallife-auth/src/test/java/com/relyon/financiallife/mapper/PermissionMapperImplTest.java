package com.relyon.financiallife.mapper;

import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.dto.PermissionRequest;
import com.relyon.financiallife.model.permissions.dto.PermissionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PermissionMapperImplTest {

    @InjectMocks
    private PermissionMapperImpl permissionMapper;

    @Test
    void permissionRequestToPermissionModel_ShouldReturnPermissionModel() {
        PermissionRequest permissionRequest = new PermissionRequest("user", "create");

        Permission permission = permissionMapper.permissionRequestToPermissionModel(permissionRequest);

        assertNotNull(permission);
        assertEquals("user:create", permission.getName());
    }

    @Test
    void permissionToPermissionResponse_shouldReturnPermissionResponse() {
        Permission permission = new Permission("user:create");
        permission.setId(1);

        PermissionResponse permissionResponse = permissionMapper.permissionToPermissionResponse(permission);

        assertNotNull(permissionResponse);
        assertEquals(1, permissionResponse.getId());
        assertEquals("user:create", permissionResponse.getName());
    }
}