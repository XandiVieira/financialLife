package com.relyon.financiallife.controller;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.permission.PermissionFilters;
import com.relyon.financiallife.mapper.PermissionMapper;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.dto.PermissionRequest;
import com.relyon.financiallife.model.permissions.dto.PermissionResponse;
import com.relyon.financiallife.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    @Mock
    private PermissionService permissionService;
    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionController permissionController;

    @Test
    void createPermissions_WithValidRequest_ShouldReturn201() {
        List<PermissionRequest> permissionsRequest = List.of(new PermissionRequest("user", "create"));
        List<Permission> permissions = List.of(new Permission(1, "user:create"));
        List<PermissionResponse> permissionResponse = List.of(new PermissionResponse(1, "user:create"));

        when(permissionMapper.permissionRequestToPermissionModel(permissionsRequest.get(0))).thenReturn(permissions.get(0));
        when(permissionService.createPermissions(permissions)).thenReturn(permissions);
        when(permissionMapper.permissionToPermissionResponse(permissions.get(0))).thenReturn(permissionResponse.get(0));

        ResponseEntity<List<PermissionResponse>> result = permissionController.createPermissions(permissionsRequest);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(permissionResponse, result.getBody());
        verify(permissionMapper, times(1)).permissionRequestToPermissionModel(permissionsRequest.get(0));
        verify(permissionMapper, times(1)).permissionToPermissionResponse(permissions.get(0));
        verify(permissionService, times(1)).createPermissions(permissions);
    }

    @Test
    void getAllPermissions_ShouldReturn200() {
        Pagination pagination = new Pagination();
        PermissionFilters permissionFilters = new PermissionFilters();
        BaseSort baseSort = new BaseSort();
        Page<Permission> mockedPage = new PageImpl<>(Arrays.asList(new Permission(), new Permission()));

        when(permissionService.getAllPermissions(pagination, permissionFilters, baseSort)).thenReturn(mockedPage);
        when(permissionMapper.permissionToPermissionResponse(any(Permission.class))).thenReturn(new PermissionResponse());

        ResponseEntity<Page<PermissionResponse>> responseEntity = permissionController.getAllPermissions(pagination, permissionFilters, baseSort);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(2, Objects.requireNonNull(responseEntity.getBody()).getContent().size());
        verify(permissionMapper, times(2)).permissionToPermissionResponse(any(Permission.class));
    }

    @Test
    void getPermissionById_ShouldReturnCorrectPermission() {
        Permission expectedPermission = new Permission(1, "user:view");

        when(permissionService.getAllPermissionsByIds(Collections.singletonList(1))).thenReturn(Collections.singletonList(expectedPermission));
        when(permissionMapper.permissionToPermissionResponse(expectedPermission)).thenReturn(new PermissionResponse(expectedPermission.getId(), expectedPermission.getName()));

        ResponseEntity<PermissionResponse> actualPermission = permissionController.getPermissionById(1);

        assertEquals(expectedPermission.getName(), Objects.requireNonNull(actualPermission.getBody()).getName());
        verify(permissionService, times(1)).getAllPermissionsByIds(Collections.singletonList(1));
        verify(permissionMapper, times(1)).permissionToPermissionResponse(expectedPermission);
    }

    @Test
    void updatePermissions_WithValidRequest_ShouldReturn200() {
        PermissionRequest permissionRequest = new PermissionRequest("user", "create");
        Integer permissionId = 1;
        Permission permission = new Permission(permissionId, "user:create");
        PermissionResponse permissionResponse = new PermissionResponse(permissionId, "user:create");

        when(permissionMapper.permissionRequestToPermissionModel(permissionRequest)).thenReturn(permission);
        when(permissionService.updatePermission(permissionId, permission)).thenReturn(permission);
        when(permissionMapper.permissionToPermissionResponse(permission)).thenReturn(permissionResponse);

        ResponseEntity<PermissionResponse> result = permissionController.updatePermission(permissionId, permissionRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(permissionResponse, result.getBody());
        verify(permissionMapper, times(permissionId)).permissionRequestToPermissionModel(permissionRequest);
        verify(permissionMapper, times(permissionId)).permissionToPermissionResponse(permission);
        verify(permissionService, times(permissionId)).updatePermission(permissionId, permission);
    }

    @Test
    void deletePermission_ShouldDeletePermissionSuccessfully() {
        Integer permissionId = 1;
        doNothing().when(permissionService).deletePermission(permissionId);

        ResponseEntity<Void> voidResponseEntity = permissionController.deletePermission(permissionId);

        assertEquals(HttpStatus.NO_CONTENT, voidResponseEntity.getStatusCode());
        verify(permissionService, times(1)).deletePermission(permissionId);
    }
}