package com.relyon.financiallife.mapper;

import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.dto.PermissionRequest;
import com.relyon.financiallife.model.permissions.dto.PermissionResponse;
import org.mapstruct.Mapper;

@Mapper
public interface PermissionMapper {
    Permission permissionRequestToPermissionModel(PermissionRequest permissionRequest);

    PermissionResponse permissionToPermissionResponse(Permission permission);
}