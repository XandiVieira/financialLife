package com.relyon.financiallife.service;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.role.RoleFilters;
import com.relyon.financiallife.exception.custom.AuthenticationFailedException;
import com.relyon.financiallife.exception.custom.BadRequestException;
import com.relyon.financiallife.exception.custom.ForbiddenException;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.repository.RoleRepository;
import com.relyon.financiallife.repository.specification.RoleSpecification;
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
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private RoleService roleService;

    @Test
    void createRoles_WithValidRequest_ShouldReturnListOfCreatedRoles() {
        List<Role> rolesRequest = getRolesRequest();
        List<Role> roleResponse = getRolesResponse();

        when(roleRepository.saveAll(rolesRequest)).thenReturn(roleResponse);

        List<Role> response = roleService.createRoles(rolesRequest);

        assertEquals(roleResponse, response);
        verify(roleRepository, times(1)).saveAll(rolesRequest);
    }

    @Test
    void createRoles_WithForbiddenPermissions_ShouldThrowForbiddenException() {
        List<Role> rolesRequest = getRolesRequest();
        rolesRequest.get(0).setPermissions(List.of(new Permission(1, "permission:create")));

        assertThrows(ForbiddenException.class, () -> roleService.createRoles(rolesRequest));
    }

    @Test
    void createRole_WithNullRequest_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> roleService.createRoles(null));
        verify(roleRepository, never()).saveAll(anyList());
    }

    @Test
    void createRole_WithEmptyRequest_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> roleService.createRoles(Collections.emptyList()));
        verify(roleRepository, never()).saveAll(anyList());
    }

    @Test
    void getAllRoles_WithAdminRole_ShouldReturnListOfRoles() {
        List<Role> roles = getRolesRequest();
        Page<Role> page = new PageImpl<>(roles);
        Pagination pagination = new Pagination(0, 10);
        RoleFilters roleFilters = new RoleFilters();
        BaseSort baseSort = new BaseSort("createdBy,-name");
        Specification<Role> roleSpecification = buildRoleSpecification(roleFilters);
        Pageable pageable = buildPagination(pagination, baseSort);

        setAuthenticationWithRole("ROLE_ADMIN");

        when(roleRepository.findAll(roleSpecification, pageable)).thenReturn(page);

        Page<Role> rolePage = roleService.getAllRoles(pagination, roleFilters, baseSort);

        assertEquals(2, rolePage.getContent().size());
        assertEquals("ROLE_ADMIN", rolePage.getContent().get(0).getName());
        assertEquals("ROLE_MANAGER", rolePage.getContent().get(1).getName());
        verify(roleRepository, times(1)).findAll(roleSpecification, pageable);
    }

    @Test
    void getAllRoles_WithNonAdminRole_ShouldReturnListOfNonForbiddenRoles() {
        List<Role> roles = getRolesRequest();
        Page<Role> page = new PageImpl<>(roles);
        Pagination pagination = new Pagination(0, 10);
        RoleFilters roleFilters = new RoleFilters();
        BaseSort baseSort = new BaseSort("createdBy,-name");
        Specification<Role> roleSpecification = buildRoleSpecification(roleFilters);
        Pageable pageable = buildPagination(pagination, baseSort);

        setAuthenticationWithRole("ROLE_MANAGER");

        when(roleRepository.findAll(roleSpecification, pageable)).thenReturn(page);

        Page<Role> rolePage = roleService.getAllRoles(pagination, roleFilters, baseSort);

        assertEquals(1, rolePage.getContent().size());
        assertEquals("ROLE_MANAGER", rolePage.getContent().get(0).getName());
        verify(roleRepository, times(1)).findAll(roleSpecification, pageable);
    }

    @Test
    void getAllRoles_WithNullAuthentication_ShouldThrowAuthenticationFailedException() {
        Pagination pagination = new Pagination(0, 10);
        RoleFilters roleFilters = new RoleFilters();
        BaseSort baseSort = new BaseSort("createdBy,-name");

        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(AuthenticationFailedException.class, () -> roleService.getAllRoles(pagination, roleFilters, baseSort));
    }

    @Test
    void getAllRolesByIds_AsAdmin_ShouldReturnListOfRolesByIds() {
        List<Integer> roleIds = List.of(1, 2, 3);
        List<Role> roles = List.of(new Role(1, "ROLE_ADMIN"), new Role(2, "ROLE_MANAGER"));

        setAuthenticationWithRole("ROLE_ADMIN");

        when(roleRepository.findAllById(roleIds)).thenReturn(roles);

        List<Role> result = roleService.getAllRolesByIds(roleIds);

        assertEquals(roles, result);
        verify(roleRepository).findAllById(roleIds);
    }

    @Test
    void getAllRolesByIds_AsNonAdmin_ShouldReturnListOfNonForbiddenRolesByIds() {
        List<Integer> roleIds = List.of(1, 2, 3);
        List<Role> roles = List.of(new Role(1, "ROLE_ADMIN"), new Role(2, "ROLE_MANAGER"), new Role(3, "role:view"));

        setAuthenticationWithRole("ROLE_MANAGER");

        when(roleRepository.findByIdsExceptAdmin(roleIds)).thenReturn(roles);

        List<Role> result = roleService.getAllRolesByIds(roleIds);

        assertEquals(roles, result);
        verify(roleRepository).findByIdsExceptAdmin(roleIds);
    }

    @Test
    void getAllRolesByIds_AsNonAdmin_WithForbiddenIds_ShouldThrowForbiddenException() {
        List<Integer> roleIds = List.of(1, 2, 3);
        List<Role> roles = List.of(new Role(1, "user:view"), new Role(2, "user:create"));

        setAuthenticationWithRole("ROLE_MANAGER");

        when(roleRepository.findByIdsExceptAdmin(roleIds)).thenReturn(roles);

        assertThrows(ForbiddenException.class, () -> roleService.getAllRolesByIds(roleIds));
        verify(roleRepository, times(1)).findByIdsExceptAdmin(roleIds);
    }

    @Test
    void getAllRolesByIds_AsAdmin_WithNonExistentIds_ShouldThrowNotFoundException() {
        List<Integer> roleIds = List.of(20, 25, 30);
        List<Role> roles = Collections.emptyList();

        setAuthenticationWithRole("ROLE_ADMIN");

        when(roleRepository.findAllById(roleIds)).thenReturn(roles);

        assertThrows(NotFoundException.class, () -> roleService.getAllRolesByIds(roleIds));
        verify(roleRepository, times(1)).findAllById(roleIds);
    }

    @Test
    void getAllRolesByIds_WithEmptyIdsList_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> roleService.getAllRolesByIds(Collections.emptyList()));
    }

    @Test
    void getAllRolesByIds_WithNullIdsList_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> roleService.getAllRolesByIds(null));
    }

    @Test
    void updateRole_WithValidRequest_ShouldReturnUpdatedRole() {
        Integer id = 1;
        Role roleRequest = new Role();
        roleRequest.setName("New Role Name");

        Role existingRole = new Role();
        existingRole.setId(id);
        existingRole.setName("Existing Role Name");

        when(roleRepository.findById(id)).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(roleRequest);

        Role updatedRole = roleService.updateRole(id, roleRequest);

        assertEquals(roleRequest.getName(), updatedRole.getName());
        verify(roleRepository, times(1)).findById(id);
        verify(roleRepository, times(1)).save(roleRequest);
    }

    @Test
    void updateRole_WithForbiddenPermission_ShouldThrowForbiddenException() {
        Integer id = 1;
        Role roleRequest = new Role();
        roleRequest.setName("New Role Name");
        roleRequest.setPermissions(List.of(new Permission(1, "permission:update")));

        Role existingRole = new Role();
        existingRole.setId(id);
        existingRole.setName("Existing Role Name");

        when(roleRepository.findById(id)).thenReturn(Optional.of(existingRole));
        assertThrows(ForbiddenException.class, () -> roleService.updateRole(id, roleRequest));
    }

    @Test
    void updateRole_WithProtectedRole_ShouldReturnUpdatedRole() {
        Integer id = 1;
        Role roleRequest = new Role();
        roleRequest.setName("ROLE_ADMIN");

        Role existingRole = new Role();
        existingRole.setId(id);
        existingRole.setName("ROLE_ADMIN");

        when(roleRepository.findById(id)).thenReturn(Optional.of(existingRole));

        assertThrows(ForbiddenException.class, () -> roleService.updateRole(id, roleRequest));
        verify(roleRepository, never()).save(any());
    }

    @Test
    void deleteRole_WithProtectedRole_ShouldThrowForbiddenException() {
        Integer roleId = 1;
        Role protectedRole = new Role();
        protectedRole.setId(roleId);
        protectedRole.setName("ROLE_ADMIN");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(protectedRole));

        assertThrows(ForbiddenException.class, () -> roleService.deleteRole(roleId));
        verify(roleRepository, never()).deleteById(roleId);
    }

    @Test
    void deleteRole_WithUnprotectedRole_ShouldDeleteRoleSuccessfully() {
        Integer roleId = 1;
        Role unprotectedRole = new Role();
        unprotectedRole.setId(roleId);
        unprotectedRole.setName("USER");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(unprotectedRole));

        roleService.deleteRole(roleId);
        verify(roleRepository).deleteById(roleId);
    }

    @Test
    void deleteRole_WithNonExistentId_ShouldThrowNotFoundException() {
        Integer roleId = 1;

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.deleteRole(roleId));
        verify(roleRepository, never()).deleteById(any());
    }

    private static List<Role> getRolesRequest() {
        List<Role> roles = new ArrayList<>();
        roles.add(new Role("ROLE_ADMIN"));
        roles.add(new Role("ROLE_MANAGER"));
        return roles;
    }

    private static List<Role> getRolesResponse() {
        List<Role> roles = new ArrayList<>();
        roles.add(new Role(1, "ROLE_ADMIN"));
        roles.add(new Role(2, "ROLE_MANAGER"));
        return roles;
    }

    private void setAuthenticationWithRole(String ROLE_ADMIN) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        doReturn(authorities).when(authentication).getAuthorities();
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

    private static Specification<Role> buildRoleSpecification(RoleFilters roleFilters) {
        return RoleSpecification.builder()
                .name(roleFilters.getName())
                .createdBy(roleFilters.getCreatedBy())
                .lastModifiedBy(roleFilters.getLastModifiedBy())
                .build();
    }
}