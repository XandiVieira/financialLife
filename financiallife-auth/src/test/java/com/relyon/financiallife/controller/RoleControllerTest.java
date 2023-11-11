package com.relyon.financiallife.controller;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.role.RoleFilters;
import com.relyon.financiallife.mapper.RoleMapper;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.dto.PermissionResponse;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.dto.RoleRequest;
import com.relyon.financiallife.model.role.dto.RoleResponse;
import com.relyon.financiallife.service.PermissionService;
import com.relyon.financiallife.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private RoleController roleController;

    @Test
    void createRoles_WithValidRequest_ShouldReturn201() {
        ArrayList<Integer> permissionsIds = new ArrayList<>();
        ArrayList<PermissionResponse> permissionsResponse = new ArrayList<>();
        ArrayList<Permission> permissions = new ArrayList<>();
        List<RoleRequest> rolesRequest = List.of(new RoleRequest("user", permissionsIds));
        List<Role> roles = List.of(new Role(1, "ROLE_ADMIN"));
        List<RoleResponse> roleResponse = List.of(new RoleResponse(1, "ROLE_ADMIN", permissionsResponse));

        when(roleMapper.roleRequestToRoleModel(rolesRequest.get(0), permissions)).thenReturn(roles.get(0));
        when(permissionService.getAllPermissionsByIds(permissionsIds)).thenReturn(permissions);
        when(roleService.createRoles(roles)).thenReturn(roles);
        when(roleMapper.roleToRoleResponse(roles.get(0))).thenReturn(roleResponse.get(0));

        ResponseEntity<List<RoleResponse>> result = roleController.createRoles(rolesRequest);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(roleResponse, result.getBody());
        verify(roleMapper, times(1)).roleRequestToRoleModel(rolesRequest.get(0), new ArrayList<>());
        verify(roleMapper, times(1)).roleToRoleResponse(roles.get(0));
        verify(roleService, times(1)).createRoles(roles);
    }

    @Test
    void getAllRoles_ShouldReturn200() {
        Pagination pagination = new Pagination();
        RoleFilters roleFilters = new RoleFilters();
        BaseSort baseSort = new BaseSort();
        Page<Role> mockedPage = new PageImpl<>(Arrays.asList(new Role(), new Role()));

        when(roleService.getAllRoles(pagination, roleFilters, baseSort)).thenReturn(mockedPage);
        when(roleMapper.roleToRoleResponse(any(Role.class))).thenReturn(new RoleResponse());

        ResponseEntity<Page<RoleResponse>> responseEntity = roleController.getAllRoles(pagination, roleFilters, baseSort);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(2, Objects.requireNonNull(responseEntity.getBody()).getContent().size());
        verify(roleMapper, times(2)).roleToRoleResponse(any(Role.class));
    }

    @Test
    void getRoleById_ShouldReturnCorrectRole() {
        Role expectedRole = new Role(1, "user:view");

        when(roleService.getAllRolesByIds(Collections.singletonList(1))).thenReturn(Collections.singletonList(expectedRole));
        when(roleMapper.roleToRoleResponse(expectedRole)).thenReturn(new RoleResponse(expectedRole.getId(), expectedRole.getName(), new ArrayList<>()));

        ResponseEntity<RoleResponse> actualRole = roleController.getRoleById(1);

        assertEquals(expectedRole.getName(), Objects.requireNonNull(actualRole.getBody()).getName());
        verify(roleService, times(1)).getAllRolesByIds(Collections.singletonList(1));
        verify(roleMapper, times(1)).roleToRoleResponse(expectedRole);
    }

    @Test
    void updateRoles_WithValidRequest_ShouldReturn200() {
        RoleRequest roleRequest = new RoleRequest("ROLE_ADMIN", new ArrayList<>());
        Integer roleId = 1;
        Role role = new Role(roleId, "ROLE_ADMIN");
        RoleResponse roleResponse = new RoleResponse(roleId, "ROLE_ADMIN", new ArrayList<>());

        when(roleMapper.roleRequestToRoleModel(roleRequest, new ArrayList<>())).thenReturn(role);
        when(roleService.updateRole(roleId, role)).thenReturn(role);
        when(roleMapper.roleToRoleResponse(role)).thenReturn(roleResponse);

        ResponseEntity<RoleResponse> result = roleController.updateRole(roleId, roleRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(roleResponse, result.getBody());
        verify(roleMapper, times(roleId)).roleRequestToRoleModel(roleRequest, new ArrayList<>());
        verify(roleMapper, times(roleId)).roleToRoleResponse(role);
        verify(roleService, times(roleId)).updateRole(roleId, role);
    }

    @Test
    void deleteRole_ShouldDeleteRoleSuccessfully() {
        Integer roleId = 1;
        doNothing().when(roleService).deleteRole(roleId);

        ResponseEntity<Void> voidResponseEntity = roleController.deleteRole(roleId);

        assertEquals(HttpStatus.NO_CONTENT, voidResponseEntity.getStatusCode());
        verify(roleService, times(1)).deleteRole(roleId);
    }
}