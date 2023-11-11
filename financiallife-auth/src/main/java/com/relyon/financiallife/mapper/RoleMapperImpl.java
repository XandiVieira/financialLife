package com.relyon.financiallife.mapper;

import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.dto.PermissionResponse;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.dto.RoleRequest;
import com.relyon.financiallife.model.role.dto.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleMapperImpl implements RoleMapper {

    private static final String ROLE_SUFFIX = "ROLE_";

    private final PermissionMapper permissionMapper;

    @Override
    public Role roleRequestToRoleModel(RoleRequest roleRequest, List<Permission> permissions) {
        return new Role(formatRoleName(roleRequest), permissions);
    }

    @Override
    public RoleResponse roleToRoleResponse(Role role) {
        List<PermissionResponse> permissionResponse = new ArrayList<>();
        if (role.getPermissions() != null) {
            permissionResponse = role.getPermissions().stream().map(permissionMapper::permissionToPermissionResponse).toList();
        }
        return new RoleResponse(role.getId(), convertRoleIntoString(role), permissionResponse);
    }

    private static String convertRoleIntoString(Role role) {
        return role.getName().replace(ROLE_SUFFIX, "").toLowerCase();
    }

    private static String formatRoleName(RoleRequest roleRequest) {
        roleRequest.setName(roleRequest.getName().replace(ROLE_SUFFIX, ""));
        return ROLE_SUFFIX + roleRequest.getName().toUpperCase();
    }
}