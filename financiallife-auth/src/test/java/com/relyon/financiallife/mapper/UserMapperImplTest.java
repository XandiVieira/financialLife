package com.relyon.financiallife.mapper;

import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.dto.RoleResponse;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.UserExtras;
import com.relyon.financiallife.model.user.dto.request.CreateUserRequest;
import com.relyon.financiallife.model.user.dto.request.UpdateUserRequest;
import com.relyon.financiallife.model.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMapperImplTest {

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private UserMapperImpl userMapper;

    @Test
    void createUserRequestToUserModel_ShouldReturnUserModel() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setFirstName("John");
        createUserRequest.setLastName("Doe");
        createUserRequest.setUsername("johndoe");
        createUserRequest.setCpf("12345678901");
        createUserRequest.setCellphoneNumber("555-5555");
        createUserRequest.setEmail("johndoe@example.com");

        Role role = new Role("ROLE_ADMIN");
        List<Role> roles = List.of(role);

        User user = userMapper.createUserRequestToUserModel(createUserRequest, roles);

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("12345678901", user.getCpf());
        assertEquals("555-5555", user.getCellphoneNumber());
        assertEquals("johndoe@example.com", user.getEmail());
        assertEquals(roles, user.getRoles());
    }

    @Test
    void updateUserRequestToUserModel_ShouldReturnUserModel() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("Jane");
        updateUserRequest.setLastName("Doe");
        updateUserRequest.setUsername("janedoe");
        updateUserRequest.setDateOfBirth(LocalDate.now());
        updateUserRequest.setCpf("12345678901");
        updateUserRequest.setCellphoneNumber("555-5555");
        updateUserRequest.setEmail("janedoe@example.com");
        updateUserRequest.setEnabled(true);

        Role role = new Role("ROLE_USER");
        List<Role> roles = List.of(role);

        User user = userMapper.updateUserRequestToUserModel(updateUserRequest, roles);

        assertEquals("Jane", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("janedoe", user.getUsername());
        assertEquals(LocalDate.now(), user.getDateOfBirth());
        assertEquals("12345678901", user.getCpf());
        assertEquals("555-5555", user.getCellphoneNumber());
        assertEquals("janedoe@example.com", user.getEmail());
        assertTrue(user.isEnabled());
        assertEquals(roles, user.getRoles());
    }

    @Test
    void userToUserResponse_ShouldReturnUserResponse() {
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .dateOfBirth(LocalDate.parse("30/06/1997", DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .cpf("12345678900")
                .cellphoneNumber("555-555-5555")
                .email("johndoe@example.com")
                .password("password")
                .enabled(true)
                .userExtras(UserExtras.builder().loginAttempts(0).lastLogin(LocalDateTime.now()).passwordRedefinitionAttempts(0).passwordRedefinitionBlockExpirationTime(LocalDateTime.now()).build())
                .build();

        Role role = new Role("ROLE_ADMIN");
        user.setRoles(List.of(role));

        RoleResponse roleResponse = new RoleResponse(1, "admin", Collections.emptyList());

        when(roleMapper.roleToRoleResponse(role)).thenReturn(roleResponse);

        UserResponse userResponse = userMapper.userToUserResponse(user);

        assertEquals(1L, userResponse.getId());
        assertEquals("John", userResponse.getFirstName());
        assertEquals("Doe", userResponse.getLastName());
        assertEquals("johndoe", userResponse.getUsername());
        assertEquals("30/06/1997", userResponse.getDateOfBirth());
        assertEquals("12345678900", userResponse.getCpf());
        assertEquals("555-555-5555", userResponse.getCellphoneNumber());
        assertEquals("johndoe@example.com", userResponse.getEmail());
        assertTrue(userResponse.isEnabled());
        assertEquals(1, userResponse.getRoles().size());
        assertEquals(1, userResponse.getRoles().get(0).getId());
        assertEquals("admin", userResponse.getRoles().get(0).getName());
        verify(roleMapper, times(1)).roleToRoleResponse(role);
    }
}