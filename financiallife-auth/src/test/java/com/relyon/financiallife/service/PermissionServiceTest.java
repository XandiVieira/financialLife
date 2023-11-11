package com.relyon.financiallife.service;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.permission.PermissionFilters;
import com.relyon.financiallife.exception.custom.AuthenticationFailedException;
import com.relyon.financiallife.exception.custom.BadRequestException;
import com.relyon.financiallife.exception.custom.ForbiddenException;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.repository.PermissionRepository;
import com.relyon.financiallife.repository.specification.PermissionSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.webjars.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void createPermissions_WithValidRequest_ShouldReturnListOfPermissionsCreated() {
        List<Permission> permissionsRequest = getPermissionsRequest();
        List<Permission> permissionResponse = getPermissionsResponse();

        when(permissionRepository.saveAll(permissionsRequest)).thenReturn(permissionResponse);

        List<Permission> response = permissionService.createPermissions(permissionsRequest);

        assertEquals(permissionResponse, response);
        verify(permissionRepository, times(1)).saveAll(permissionsRequest);
    }

    @Test
    void createPermission_WithNullRequest_ShouldThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> permissionService.createPermissions(null));
        verify(permissionRepository, never()).saveAll(anyList());
    }

    @Test
    void createPermission_WithEmptyRequest_ShouldThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> permissionService.createPermissions(Collections.emptyList()));
        verify(permissionRepository, never()).saveAll(anyList());
    }

    @Test
    void getAllPermissions_WithAdminRole_ShouldReturnListOfPermissions() {
        List<Permission> permissions = getPermissionsRequest();
        Page<Permission> page = new PageImpl<>(permissions);
        Pagination pagination = new Pagination(0, 10);
        PermissionFilters permissionFilters = new PermissionFilters();
        BaseSort baseSort = new BaseSort("createdBy,-name");
        Specification<Permission> permissionSpecification = buildPermissionSpecification(permissionFilters);
        Pageable pageable = buildPagination(pagination, baseSort);

        setAuthenticationWithRole("ROLE_ADMIN");

        when(permissionRepository.findAll(permissionSpecification, pageable)).thenReturn(page);

        Page<Permission> permissionPage = permissionService.getAllPermissions(pagination, permissionFilters, baseSort);

        assertEquals(2, permissionPage.getContent().size());
        assertEquals("user:view", permissionPage.getContent().get(0).getName());
        assertEquals("user:create", permissionPage.getContent().get(1).getName());
        verify(permissionRepository, times(1)).findAll(permissionSpecification, pageable);
    }

    @Test
    void getAllPermissions_WithNonAdminRole_ShouldReturnListOfNonForbiddenPermissions() {
        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(1, "user:view"));
        permissions.add(new Permission(2, "user:create"));
        Page<Permission> page = new PageImpl<>(permissions);
        Pagination pagination = new Pagination(0, 10);
        PermissionFilters permissionFilters = new PermissionFilters();
        BaseSort baseSort = new BaseSort("createdBy,-name");
        Specification<Permission> permissionSpecification = buildPermissionSpecification(permissionFilters);
        Pageable pageable = buildPagination(pagination, baseSort);

        setAuthenticationWithRole("ROLE_MANAGER");

        when(permissionRepository.findAll(permissionSpecification, pageable)).thenReturn(page);

        Page<Permission> permissionPage = permissionService.getAllPermissions(pagination, permissionFilters, baseSort);

        assertEquals(2, permissionPage.getContent().size());
        assertEquals("user:view", permissionPage.getContent().get(0).getName());
        assertEquals("user:create", permissionPage.getContent().get(1).getName());
        verify(permissionRepository, times(1)).findAll(permissionSpecification, pageable);
    }

    @Test
    void getAllPermissions_WithNullAuthentication_ShouldThrowAuthenticationFailedException() {
        Pagination pagination = new Pagination(0, 10);
        PermissionFilters permissionFilters = new PermissionFilters();
        BaseSort baseSort = new BaseSort("createdBy,-name");

        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(AuthenticationFailedException.class, () -> permissionService.getAllPermissions(pagination, permissionFilters, baseSort));
    }

    @Test
    void getAllPermissionsByIds_AsAdmin_ShouldReturnListOfPermissionsByIds() {
        List<Integer> permissionIds = List.of(1, 2, 3);
        List<Permission> permissions = List.of(new Permission(1, "user:view"), new Permission(2, "user:create"), new Permission(3, "permission:view"));

        setAuthenticationWithRole("ROLE_ADMIN");

        when(permissionRepository.findAllById(permissionIds)).thenReturn(permissions);

        List<Permission> result = permissionService.getAllPermissionsByIds(permissionIds);

        assertEquals(permissions, result);
        verify(permissionRepository).findAllById(permissionIds);
    }

    @Test
    void getAllPermissionsByIds_AsNonAdmin_ShouldReturnListOfNonForbiddenPermissionsByIds() {
        List<Integer> permissionIds = List.of(1, 2, 3);
        List<Permission> permissions = List.of(new Permission(1, "user:view"), new Permission(2, "user:create"), new Permission(3, "permission:view"));

        setAuthenticationWithRole("ROLE_MANAGER");

        when(permissionRepository.findByIdsByRoleManager(permissionIds)).thenReturn(permissions);

        List<Permission> result = permissionService.getAllPermissionsByIds(permissionIds);

        assertEquals(permissions, result);
        verify(permissionRepository).findByIdsByRoleManager(permissionIds);
    }

    @Test
    void getAllPermissionsByIds_AsNonAdmin_WithForbiddenIds_ShouldThrowForbiddenException() {
        List<Integer> permissionIds = List.of(1, 2, 3);
        List<Permission> permissions = List.of(new Permission(1, "user:view"), new Permission(2, "user:create"));

        setAuthenticationWithRole("ROLE_MANAGER");

        when(permissionRepository.findByIdsByRoleManager(permissionIds)).thenReturn(permissions);

        assertThrows(ForbiddenException.class, () -> permissionService.getAllPermissionsByIds(permissionIds));
        verify(permissionRepository, times(1)).findByIdsByRoleManager(permissionIds);
    }

    @Test
    void getAllPermissionsByIds_AsAdmin_WithNonExistentIds_ShouldThrowNotFoundException() {
        List<Integer> permissionIds = List.of(20, 25, 30);
        List<Permission> permissions = Collections.emptyList();

        setAuthenticationWithRole("ROLE_ADMIN");

        when(permissionRepository.findAllById(permissionIds)).thenReturn(permissions);

        assertThrows(NotFoundException.class, () -> permissionService.getAllPermissionsByIds(permissionIds));
        verify(permissionRepository, times(1)).findAllById(permissionIds);
    }

    @Test
    void getAllPermissionsByIds_WithEmptyIdsList_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> permissionService.getAllPermissionsByIds(Collections.emptyList()));
    }

    @Test
    void getAllPermissionsByIds_WithNullIdsList_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> permissionService.getAllPermissionsByIds(null));
    }

    @Test
    void updatePermission_WithValidId_ShouldReturnUpdatedUser() {
        int id = 1;
        Permission permission = new Permission();
        permission.setId(id);
        Permission existingPermission = new Permission();
        existingPermission.setId(id);
        Permission updatedPermission = new Permission();
        updatedPermission.setId(id);
        Permission permissionResponse = new Permission();
        permissionResponse.setId(id);

        when(permissionRepository.findById(1)).thenReturn(Optional.of(permission));
        when(permissionRepository.save(permission)).thenReturn(updatedPermission);

        Permission response = permissionService.updatePermission(id, permission);

        assertEquals(permissionResponse, response);
        verify(permissionRepository, times(1)).save(permission);
    }

    @Test
    void updateForbiddenPermission_ShouldThrowForbiddenException() {
        int permissionId = 1;
        String permissionName = "user:view";
        Permission permission = new Permission(permissionId, permissionName);

        when(permissionRepository.findById(1)).thenReturn(Optional.of(permission));

        assertThrows(ForbiddenException.class, () -> permissionService.updatePermission(permissionId, permission));
    }

    @Test
    void deletePermission_WithValidId_ShouldDeletePermissionSuccessfully() {
        Integer permissionId = 1;
        Permission permission = new Permission();
        permission.setId(permissionId);
        permission.setName("user:test");

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));

        permissionService.deletePermission(permissionId);

        verify(permissionRepository, times(1)).deleteById(permissionId);
    }

    @Test
    void deleteForbiddenPermission_WithValidId_ShouldThrowForbiddenException() {
        Integer permissionId = 1;
        Permission permission = new Permission();
        permission.setId(permissionId);
        permission.setName("user:view");

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));

        assertThrows(ForbiddenException.class, () -> permissionService.deletePermission(permissionId));
    }

    @Test
    void deletePermission_WithNonExistentId_ShouldThrowNotFoundException() {
        Integer permissionId = 1;

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> permissionService.deletePermission(permissionId));
        verify(permissionRepository, never()).deleteById(any());
    }

    private static PageRequest buildPagination(Pagination pagination, BaseSort baseSort) {
        return PageRequest.of(
                pagination.getPageNumber(),
                pagination.getPageSize(),
                org.springframework.data.domain.Sort.by(getSort(baseSort.getSort()))
        );
    }

    private static List<org.springframework.data.domain.Sort.Order> getSort(String sort) {
        List<org.springframework.data.domain.Sort.Order> orders = new ArrayList<>();
        for (String field : sort.split(",")) {
            if (field.startsWith("-")) {
                orders.add(new org.springframework.data.domain.Sort.Order(org.springframework.data.domain.Sort.Direction.DESC, field.substring(1)));
            } else {
                orders.add(new org.springframework.data.domain.Sort.Order(org.springframework.data.domain.Sort.Direction.ASC, field));
            }
        }
        return orders;
    }

    private static Specification<Permission> buildPermissionSpecification(PermissionFilters permissionFilters) {
        return PermissionSpecification.builder()
                .name(permissionFilters.getName())
                .createdBy(permissionFilters.getCreatedBy())
                .lastModifiedBy(permissionFilters.getLastModifiedBy())
                .build();
    }

    private static List<Permission> getPermissionsResponse() {
        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(1, "user:view"));
        permissions.add(new Permission(2, "user:create"));
        return permissions;
    }

    private static List<Permission> getPermissionsRequest() {
        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission("user:view"));
        permissions.add(new Permission("user:create"));
        return permissions;
    }

    private void setAuthenticationWithRole(String ROLE_ADMIN) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        doReturn(authorities).when(authentication).getAuthorities();
    }
}