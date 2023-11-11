package com.relyon.financiallife.configuration.db;

import com.relyon.financiallife.mapper.RoleMapper;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.enums.PermissionEnum;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.dto.RoleRequest;
import com.relyon.financiallife.repository.PermissionRepository;
import com.relyon.financiallife.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolesInitializerTest {

    @Mock
    private RoleMapper roleMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RolesInitializer rolesInitializer;

    @Test
    void run_ShouldCreateDefaultRoles() {
        List<Role> roles = buildRoles();
        List<RoleRequest> roleRequests = getRoleRequests();

        when(permissionRepository.findAll()).thenReturn(Arrays.stream(PermissionEnum.values()).map(PermissionEnum::getPermissionName).toList().stream().map(Permission::new).toList());
        for (int i = 0; i <= 2; i++) {
            when(roleMapper.roleRequestToRoleModel(roleRequests.get(i), Collections.emptyList())).thenReturn(roles.get(i));
            when(roleRepository.existsByName("ROLE_" + roleRequests.get(i).getName().toUpperCase())).thenReturn(false);
        }

        rolesInitializer.run();

        verify(roleRepository).saveAll(anyList());
    }

    @Test
    void run_ShouldCreateDefaultRolesWithPermissions() {
        List<Role> roles = buildRoles();
        roles.get(0).setPermissions(Arrays.asList(new Permission("user:crete"), new Permission("user:view")));
        List<RoleRequest> roleRequests = getRoleRequests();

        when(permissionRepository.findAll()).thenReturn(Arrays.stream(PermissionEnum.values()).map(PermissionEnum::getPermissionName).toList().stream().map(Permission::new).toList());
        for (int i = 0; i <= 2; i++) {
            when(roleMapper.roleRequestToRoleModel(roleRequests.get(i), Collections.emptyList())).thenReturn(roles.get(i));
            when(roleRepository.existsByName("ROLE_" + roleRequests.get(i).getName().toUpperCase())).thenReturn(false);
        }

        rolesInitializer.run();

        verify(roleRepository).saveAll(anyList());
    }

    @Test
    void run_ShouldNotCreateDefaultRoles() {
        List<Role> roles = buildRoles();
        List<RoleRequest> roleRequests = getRoleRequests();

        when(permissionRepository.findAll()).thenReturn(Arrays.stream(PermissionEnum.values()).map(PermissionEnum::getPermissionName).toList().stream().map(Permission::new).toList());
        for (int i = 0; i < roleRequests.size(); i++) {
            when(roleMapper.roleRequestToRoleModel(roleRequests.get(i), Collections.emptyList())).thenReturn(roles.get(i));
            when(roleRepository.existsByName("ROLE_" + roleRequests.get(i).getName().toUpperCase())).thenReturn(false);
        }

        rolesInitializer.run();

        verify(roleRepository, times(1)).saveAll(roles);
    }

    private List<RoleRequest> getRoleRequests() {
        return buildRoles().stream().map(role -> new RoleRequest(role.getName().replace("ROLE_", "").toLowerCase())).toList();
    }

    private List<Role> buildRoles() {
        return Stream.of(
                new Role(1, "ROLE_ADMIN"),
                new Role(2, "ROLE_MANAGER"),
                new Role(3, "ROLE_USER")).toList();
    }
}