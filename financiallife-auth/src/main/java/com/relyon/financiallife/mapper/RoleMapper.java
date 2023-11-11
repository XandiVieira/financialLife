package com.relyon.financiallife.mapper;

import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.dto.RoleRequest;
import com.relyon.financiallife.model.role.dto.RoleResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface RoleMapper {
    Role roleRequestToRoleModel(RoleRequest roleRequest, List<Permission> permissions);

    RoleResponse roleToRoleResponse(Role role);
}