package com.relyon.financiallife.mapper;

import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.dto.PermissionRequest;
import com.relyon.financiallife.model.permissions.dto.PermissionResponse;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapperImpl implements PermissionMapper {

    @Override
    public Permission permissionRequestToPermissionModel(PermissionRequest permissionRequest) {
        return new Permission(formatPermissionName(permissionRequest));
    }

    @Override
    public PermissionResponse permissionToPermissionResponse(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getName());
    }

    private static String formatPermissionName(PermissionRequest permissionRequest) {
        return permissionRequest.getResource() + ":" + permissionRequest.getAction();
    }
}