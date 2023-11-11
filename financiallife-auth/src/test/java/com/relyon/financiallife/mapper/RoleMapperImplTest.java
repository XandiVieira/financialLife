package com.relyon.financiallife.mapper;

import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.dto.PermissionResponse;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.dto.RoleRequest;
import com.relyon.financiallife.model.role.dto.RoleResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleMapperImplTest {

    @Mock
    private RoleRequest roleRequest;
    @Mock
    private PermissionMapper permissionMapper;
    @Mock
    private Role role;

    @InjectMocks
    private RoleMapperImpl roleMapper;

    @Test
    void roleRequestToRoleModel_ShouldReturnRoleModel() {
        when(roleRequest.getName()).thenReturn("ADMIN");

        Role result = roleMapper.roleRequestToRoleModel(roleRequest, Collections.singletonList(new Permission("user:view")));

        assertEquals("ROLE_ADMIN", result.getName());
    }

    @Test
    void roleToRoleResponse_ShouldReturnRoleResponse() {
        when(role.getId()).thenReturn(1);
        when(role.getName()).thenReturn("ROLE_ADMIN");
        when(role.getPermissions()).thenReturn(Collections.singletonList(new Permission("user:view")));
        when(permissionMapper.permissionToPermissionResponse(new Permission("user:view"))).thenReturn(new PermissionResponse(1, "user:view"));

        RoleResponse result = roleMapper.roleToRoleResponse(role);

        assertEquals(1, result.getId());
        assertEquals("admin", result.getName());
    }
}